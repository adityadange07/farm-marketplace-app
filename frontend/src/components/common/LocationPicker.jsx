import { useState } from 'react';
import { FiMapPin, FiNavigation } from 'react-icons/fi';
import Button from '../ui/Button';

export default function LocationPicker({ value, onChange }) {
  const [loading, setLoading] = useState(false);

  const detectLocation = () => {
    setLoading(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        onChange({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        });
        setLoading(false);
      },
      () => setLoading(false),
      { enableHighAccuracy: true }
    );
  };

  return (
    <div className="space-y-3">
      <Button
        type="button"
        variant="outline"
        onClick={detectLocation}
        loading={loading}
      >
        <FiNavigation className="mr-2" />
        Use My Location
      </Button>

      {value && (
        <p className="text-sm text-gray-500 flex items-center gap-1">
          <FiMapPin className="w-3 h-3" />
          {value.lat.toFixed(4)}, {value.lng.toFixed(4)}
        </p>
      )}

      <div className="grid grid-cols-2 gap-3">
        <input
          type="number"
          step="any"
          placeholder="Latitude"
          value={value?.lat || ''}
          onChange={(e) =>
            onChange({ ...value, lat: parseFloat(e.target.value) })
          }
          className="border rounded-lg px-3 py-2 text-sm"
        />
        <input
          type="number"
          step="any"
          placeholder="Longitude"
          value={value?.lng || ''}
          onChange={(e) =>
            onChange({ ...value, lng: parseFloat(e.target.value) })
          }
          className="border rounded-lg px-3 py-2 text-sm"
        />
      </div>
    </div>
  );
}