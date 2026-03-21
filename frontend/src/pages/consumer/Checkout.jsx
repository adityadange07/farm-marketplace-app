import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import {
  FiMapPin, FiTruck, FiCreditCard,
  FiShoppingBag, FiClock, FiCheck,
} from 'react-icons/fi';
import { useCartStore } from '../../store/cartStore';
import { useAuthStore } from '../../store/authStore';
import orderApi from '../../api/orderApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import toast from 'react-hot-toast';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLIC_KEY);

export default function Checkout() {
  return (
    <Elements stripe={stripePromise}>
      <CheckoutForm />
    </Elements>
  );
}

function CheckoutForm() {
  const navigate = useNavigate();
  const stripe = useStripe();
  const elements = useElements();

  const items = useCartStore((s) => s.items);
  const subtotal = useCartStore((s) => s.subtotal());
  const clearCart = useCartStore((s) => s.clearCart);
  const itemsByFarm = useCartStore((s) => s.itemsByFarm());
  const user = useAuthStore((s) => s.user);

  const [step, setStep] = useState(1); // 1: Address, 2: Delivery, 3: Payment
  const [deliveryType, setDeliveryType] = useState('DELIVERY');
  const [isProcessing, setIsProcessing] = useState(false);
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedSlot, setSelectedSlot] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors },
    getValues,
  } = useForm({
    defaultValues: {
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      zipCode: '',
      deliveryNotes: '',
    },
  });

  // Redirect if cart empty
  useEffect(() => {
    if (items.length === 0) {
      navigate('/shop');
      toast('Your cart is empty');
    }
  }, [items, navigate]);

  const deliveryFee = deliveryType === 'PICKUP' ? 0 : 5.99;
  const serviceFee = (subtotal * 0.05).toFixed(2);
  const tax = (subtotal * 0.08).toFixed(2);
  const total = (
    subtotal +
    deliveryFee +
    parseFloat(serviceFee) +
    parseFloat(tax)
  ).toFixed(2);

  const TIME_SLOTS = [
    '8:00 AM - 10:00 AM',
    '10:00 AM - 12:00 PM',
    '12:00 PM - 2:00 PM',
    '2:00 PM - 4:00 PM',
    '4:00 PM - 6:00 PM',
  ];

  // Get next 7 days
  const getAvailableDates = () => {
    const dates = [];
    for (let i = 1; i <= 7; i++) {
      const date = new Date();
      date.setDate(date.getDate() + i);
      dates.push(date.toISOString().split('T')[0]);
    }
    return dates;
  };

  const onSubmitOrder = async () => {
    if (!stripe || !elements) return;
    setIsProcessing(true);

    try {
      const addressValues = getValues();

      // 1. Create order on backend
      const orderData = {
        deliveryType,
        deliveryAddress: deliveryType === 'DELIVERY' ? {
          line1: addressValues.addressLine1,
          line2: addressValues.addressLine2,
          city: addressValues.city,
          state: addressValues.state,
          zipCode: addressValues.zipCode,
        } : null,
        deliveryNotes: addressValues.deliveryNotes,
        scheduledDate: selectedDate,
        timeSlot: selectedSlot,
      };

      const result = await orderApi.create(orderData);
      const clientSecret = result.clientSecret;

      // 2. Confirm Stripe payment
      const { error, paymentIntent } = await stripe.confirmCardPayment(
        clientSecret,
        {
          payment_method: {
            card: elements.getElement(CardElement),
            billing_details: {
              name: `${user.firstName} ${user.lastName}`,
              email: user.email,
            },
          },
        }
      );

      if (error) {
        toast.error(error.message);
        setIsProcessing(false);
        return;
      }

      if (paymentIntent.status === 'succeeded') {
        clearCart();
        toast.success('Order placed successfully! 🎉');
        navigate(`/orders/${result.orders[0].id}`, {
          state: { justPlaced: true },
        });
      }
    } catch (err) {
      toast.error('Failed to place order');
      setIsProcessing(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">Checkout</h1>

      {/* Progress Steps */}
      <div className="flex items-center justify-center mb-10">
        {[
          { num: 1, label: 'Address', icon: FiMapPin },
          { num: 2, label: 'Delivery', icon: FiTruck },
          { num: 3, label: 'Payment', icon: FiCreditCard },
        ].map(({ num, label, icon: Icon }, index) => (
          <div key={num} className="flex items-center">
            <div
              className={`flex items-center gap-2 px-4 py-2 rounded-full
                ${step >= num
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-100 text-gray-400'}`}
            >
              {step > num ? (
                <FiCheck className="w-4 h-4" />
              ) : (
                <Icon className="w-4 h-4" />
              )}
              <span className="text-sm font-medium hidden sm:inline">
                {label}
              </span>
            </div>
            {index < 2 && (
              <div
                className={`w-12 h-0.5 mx-2 ${
                  step > num ? 'bg-green-600' : 'bg-gray-200'
                }`}
              />
            )}
          </div>
        ))}
      </div>

      <div className="grid lg:grid-cols-3 gap-8">
        {/* Left: Form Steps */}
        <div className="lg:col-span-2 space-y-6">
          {/* Step 1: Address */}
          {step === 1 && (
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                <FiMapPin className="text-green-600" />
                Delivery Address
              </h2>

              {/* Delivery Type Toggle */}
              <div className="flex gap-4 mb-6">
                {[
                  { value: 'DELIVERY', label: '🚚 Delivery', sub: `$${deliveryFee}` },
                  { value: 'PICKUP', label: '🏪 Farm Pickup', sub: 'Free' },
                ].map((opt) => (
                  <button
                    key={opt.value}
                    onClick={() => setDeliveryType(opt.value)}
                    className={`flex-1 p-4 border-2 rounded-xl text-left
                      transition ${
                        deliveryType === opt.value
                          ? 'border-green-500 bg-green-50'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                  >
                    <p className="font-medium">{opt.label}</p>
                    <p className="text-sm text-gray-500 mt-1">{opt.sub}</p>
                  </button>
                ))}
              </div>

              {deliveryType === 'DELIVERY' && (
                <div className="space-y-4">
                  <Input
                    label="Address Line 1 *"
                    placeholder="123 Main Street"
                    {...register('addressLine1', { required: 'Required' })}
                    error={errors.addressLine1?.message}
                  />
                  <Input
                    label="Address Line 2"
                    placeholder="Apt, Suite (optional)"
                    {...register('addressLine2')}
                  />
                  <div className="grid grid-cols-3 gap-4">
                    <Input
                      label="City *"
                      {...register('city', { required: 'Required' })}
                      error={errors.city?.message}
                    />
                    <Input
                      label="State *"
                      {...register('state', { required: 'Required' })}
                      error={errors.state?.message}
                    />
                    <Input
                      label="ZIP *"
                      {...register('zipCode', { required: 'Required' })}
                      error={errors.zipCode?.message}
                    />
                  </div>
                </div>
              )}

              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Delivery Notes
                </label>
                <textarea
                  rows={2}
                  placeholder="Gate code, leave at door, etc."
                  className="w-full border rounded-lg px-3 py-2 text-sm"
                  {...register('deliveryNotes')}
                />
              </div>

              <div className="mt-6 flex justify-end">
                <Button onClick={() => setStep(2)}>
                  Continue to Delivery Schedule
                </Button>
              </div>
            </div>
          )}

          {/* Step 2: Delivery Schedule */}
          {step === 2 && (
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                <FiClock className="text-green-600" />
                Schedule Delivery
              </h2>

              {/* Date Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Select Date
                </label>
                <div className="flex gap-3 overflow-x-auto pb-2">
                  {getAvailableDates().map((date) => {
                    const d = new Date(date + 'T12:00:00');
                    const dayName = d.toLocaleDateString('en-US', {
                      weekday: 'short',
                    });
                    const dayNum = d.getDate();
                    const month = d.toLocaleDateString('en-US', {
                      month: 'short',
                    });

                    return (
                      <button
                        key={date}
                        onClick={() => setSelectedDate(date)}
                        className={`flex-shrink-0 w-20 p-3 rounded-xl border-2
                          text-center transition ${
                            selectedDate === date
                              ? 'border-green-500 bg-green-50'
                              : 'border-gray-200 hover:border-gray-300'
                          }`}
                      >
                        <p className="text-xs text-gray-400">{dayName}</p>
                        <p className="text-xl font-bold">{dayNum}</p>
                        <p className="text-xs text-gray-400">{month}</p>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Time Slot */}
              {selectedDate && (
                <div className="mt-6">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Select Time Slot
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    {TIME_SLOTS.map((slot) => (
                      <button
                        key={slot}
                        onClick={() => setSelectedSlot(slot)}
                        className={`p-3 rounded-xl border-2 text-sm
                          text-left transition ${
                            selectedSlot === slot
                              ? 'border-green-500 bg-green-50'
                              : 'border-gray-200 hover:border-gray-300'
                          }`}
                      >
                        <FiClock className="w-4 h-4 mb-1 text-gray-400" />
                        {slot}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              <div className="mt-6 flex justify-between">
                <Button variant="outline" onClick={() => setStep(1)}>
                  Back
                </Button>
                <Button
                  onClick={() => setStep(3)}
                  disabled={!selectedDate || !selectedSlot}
                >
                  Continue to Payment
                </Button>
              </div>
            </div>
          )}

          {/* Step 3: Payment */}
          {step === 3 && (
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                <FiCreditCard className="text-green-600" />
                Payment
              </h2>

              <div className="p-4 border rounded-xl">
                <CardElement
                  options={{
                    style: {
                      base: {
                        fontSize: '16px',
                        color: '#1f2937',
                        '::placeholder': { color: '#9ca3af' },
                      },
                    },
                  }}
                />
              </div>

              <div className="mt-4 flex items-center gap-2 text-xs text-gray-400">
                <FiCreditCard />
                Payments are securely processed by Stripe
              </div>

              <div className="mt-6 flex justify-between">
                <Button variant="outline" onClick={() => setStep(2)}>
                  Back
                </Button>
                <Button
                  size="lg"
                  loading={isProcessing}
                  onClick={onSubmitOrder}
                  disabled={!stripe}
                >
                  Pay ${total}
                </Button>
              </div>
            </div>
          )}
        </div>

        {/* Right: Order Summary */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-xl shadow-sm p-6 sticky top-24">
            <h3 className="font-semibold flex items-center gap-2 mb-4">
              <FiShoppingBag className="text-green-600" />
              Order Summary
            </h3>

            {/* Items */}
            <div className="space-y-3 max-h-60 overflow-y-auto">
              {items.map((item) => (
                <div key={item.product.id} className="flex items-center gap-3">
                  <img
                    src={
                      item.product.images?.[0]?.url ||
                      '/placeholder-product.jpg'
                    }
                    alt={item.product.name}
                    className="w-12 h-12 rounded-lg object-cover"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">
                      {item.product.name}
                    </p>
                    <p className="text-xs text-gray-400">
                      {item.quantity} × ${item.product.price.toFixed(2)}
                    </p>
                  </div>
                  <span className="text-sm font-medium">
                    ${(item.product.price * item.quantity).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>

            <div className="border-t mt-4 pt-4 space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Subtotal</span>
                <span>${subtotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Delivery</span>
                <span>
                  {deliveryFee === 0 ? (
                    <span className="text-green-600">Free</span>
                  ) : (
                    `$${deliveryFee.toFixed(2)}`
                  )}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Service Fee (5%)</span>
                <span>${serviceFee}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Tax (8%)</span>
                <span>${tax}</span>
              </div>
              <div className="border-t pt-2 flex justify-between font-bold text-lg">
                <span>Total</span>
                <span className="text-green-600">${total}</span>
              </div>
            </div>

            {/* Delivery Info */}
            {selectedDate && (
              <div className="mt-4 p-3 bg-green-50 rounded-lg text-sm">
                <p className="font-medium text-green-700">
                  📦 {deliveryType === 'PICKUP' ? 'Pickup' : 'Delivery'}
                </p>
                <p className="text-green-600">
                  {new Date(selectedDate + 'T12:00:00').toLocaleDateString(
                    'en-US',
                    {
                      weekday: 'long',
                      month: 'long',
                      day: 'numeric',
                    }
                  )}
                </p>
                <p className="text-green-600">{selectedSlot}</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}