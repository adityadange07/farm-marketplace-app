import { Fragment } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Dialog, Transition } from '@headlessui/react';
import { FiX, FiShoppingBag, FiTrash2, FiMinus, FiPlus } from 'react-icons/fi';
import { useCartStore } from '../../store/cartStore';
import { useAuthStore } from '../../store/authStore';
import Button from '../ui/Button';
import toast from 'react-hot-toast';

export default function CartDrawer() {
  const navigate = useNavigate();
  const isOpen = useCartStore((s) => s.isOpen);
  const closeCart = useCartStore((s) => s.closeCart);
  const items = useCartStore((s) => s.items);
  const updateQuantity = useCartStore((s) => s.updateQuantity);
  const removeItem = useCartStore((s) => s.removeItem);
  const clearCart = useCartStore((s) => s.clearCart);
  const subtotal = useCartStore((s) => s.subtotal());
  const totalItems = useCartStore((s) => s.totalItems());
  const itemsByFarm = useCartStore((s) => s.itemsByFarm());
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  const handleCheckout = () => {
    closeCart();
    if (!isAuthenticated) {
      toast('Please login to checkout', { icon: '🔒' });
      navigate('/login', { state: { from: { pathname: '/checkout' } } });
    } else {
      navigate('/checkout');
    }
  };

  return (
    <Transition show={isOpen} as={Fragment}>
      <Dialog onClose={closeCart} className="relative z-50">
        {/* Backdrop */}
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/40" />
        </Transition.Child>

        {/* Drawer */}
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="translate-x-full"
          enterTo="translate-x-0"
          leave="ease-in duration-200"
          leaveFrom="translate-x-0"
          leaveTo="translate-x-full"
        >
          <Dialog.Panel className="fixed right-0 top-0 bottom-0 w-full
                                   max-w-md bg-white shadow-xl flex flex-col">
            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b">
              <Dialog.Title className="text-lg font-semibold flex items-center gap-2">
                <FiShoppingBag className="text-green-600" />
                Cart ({totalItems})
              </Dialog.Title>
              <button
                onClick={closeCart}
                className="p-2 hover:bg-gray-100 rounded-full"
              >
                <FiX className="w-5 h-5" />
              </button>
            </div>

            {/* Items */}
            <div className="flex-1 overflow-y-auto px-6 py-4">
              {items.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-center">
                  <FiShoppingBag className="w-16 h-16 text-gray-200 mb-4" />
                  <p className="text-lg font-medium text-gray-400">
                    Your cart is empty
                  </p>
                  <p className="text-sm text-gray-400 mt-1">
                    Add some fresh produce!
                  </p>
                  <Button
                    variant="outline"
                    className="mt-4"
                    onClick={() => {
                      closeCart();
                      navigate('/shop');
                    }}
                  >
                    Browse Products
                  </Button>
                </div>
              ) : (
                <div className="space-y-6">
                  {/* Group by farm */}
                  {Object.entries(itemsByFarm).map(([farmId, group]) => (
                    <div key={farmId}>
                      {/* Farm header */}
                      <div className="flex items-center gap-2 mb-3">
                        <div className="w-6 h-6 bg-green-100 rounded-full
                                        flex items-center justify-center">
                          <span className="text-green-600 text-xs font-bold">
                            {group.farm?.farmName?.[0] || 'F'}
                          </span>
                        </div>
                        <span className="text-sm font-medium text-gray-600">
                          {group.farm?.farmName || 'Farm'}
                        </span>
                      </div>

                      {/* Items */}
                      <div className="space-y-3">
                        {group.items.map((item) => (
                          <CartItemRow
                            key={item.product.id}
                            item={item}
                            onUpdateQty={(qty) =>
                              updateQuantity(item.product.id, qty)
                            }
                            onRemove={() => removeItem(item.product.id)}
                          />
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Footer */}
            {items.length > 0 && (
              <div className="border-t px-6 py-4 space-y-4">
                {/* Clear Cart */}
                <button
                  onClick={clearCart}
                  className="flex items-center gap-1 text-sm text-red-500
                             hover:underline"
                >
                  <FiTrash2 className="w-3 h-3" />
                  Clear Cart
                </button>

                {/* Subtotal */}
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Subtotal</span>
                  <span className="text-xl font-bold">
                    ${subtotal.toFixed(2)}
                  </span>
                </div>
                <p className="text-xs text-gray-400">
                  Taxes & delivery calculated at checkout
                </p>

                {/* Checkout button */}
                <Button
                  className="w-full"
                  size="lg"
                  onClick={handleCheckout}
                >
                  Proceed to Checkout
                </Button>

                <button
                  onClick={() => {
                    closeCart();
                    navigate('/shop');
                  }}
                  className="w-full text-center text-sm text-green-600
                             hover:underline"
                >
                  Continue Shopping
                </button>
              </div>
            )}
          </Dialog.Panel>
        </Transition.Child>
      </Dialog>
    </Transition>
  );
}

// ── Individual Cart Item Row ─────────
function CartItemRow({ item, onUpdateQty, onRemove }) {
  const { product, quantity } = item;
  const image =
    product.images?.find((i) => i.isPrimary)?.url ||
    product.images?.[0]?.url ||
    '/placeholder-product.jpg';

  return (
    <div className="flex gap-4">
      <img
        src={image}
        alt={product.name}
        className="w-20 h-20 rounded-lg object-cover"
      />
      <div className="flex-1 min-w-0">
        <Link
          to={`/product/${product.slug}`}
          className="text-sm font-medium text-gray-900 hover:text-green-600
                     line-clamp-1"
        >
          {product.name}
        </Link>
        <p className="text-sm text-gray-400 mt-0.5">
          ${product.price.toFixed(2)} / {product.unit}
        </p>

        <div className="flex items-center justify-between mt-2">
          {/* Quantity controls */}
          <div className="flex items-center border rounded-lg">
            <button
              onClick={() => onUpdateQty(quantity - 1)}
              className="p-1.5 hover:bg-gray-50"
            >
              <FiMinus className="w-3 h-3" />
            </button>
            <span className="px-3 text-sm font-medium">{quantity}</span>
            <button
              onClick={() => onUpdateQty(quantity + 1)}
              className="p-1.5 hover:bg-gray-50"
            >
              <FiPlus className="w-3 h-3" />
            </button>
          </div>

          {/* Item total + remove */}
          <div className="flex items-center gap-3">
            <span className="text-sm font-semibold">
              ${(product.price * quantity).toFixed(2)}
            </span>
            <button
              onClick={onRemove}
              className="p-1 text-gray-400 hover:text-red-500"
            >
              <FiTrash2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}