import { useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import { Link } from 'react-router-dom';
import 'leaflet/dist/leaflet.css';

export default function FarmMap({ farms = [], center, radius = 25 }) {
  const defaultCenter = center || { lat: 40.7128, lng: -74.006 };

  return (
    <div className="h-[500px] rounded-xl overflow-hidden border">
      <MapContainer
        center={[defaultCenter.lat, defaultCenter.lng]}
        zoom={10}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; OpenStreetMap'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {center && (
          <Circle
            center={[center.lat, center.lng]}
            radius={radius * 1000}
            pathOptions={{
              color: '#16a34a',
              fillColor: '#16a34a',
              fillOpacity: 0.05,
            }}
          />
        )}

        {farms.map((farm) =>
          farm.latitude && farm.longitude ? (
            <Marker
              key={farm.id}
              position={[farm.latitude, farm.longitude]}
            >
              <Popup>
                <div className="text-center">
                  <h3 className="font-semibold">{farm.farmName}</h3>
                  <p className="text-xs text-gray-500">{farm.city}</p>
                  <Link
                    to={`/farm/${farm.id}`}
                    className="text-xs text-green-600 hover:underline"
                  >
                    View Farm →
                  </Link>
                </div>
              </Popup>
            </Marker>
          ) : null
        )}
      </MapContainer>
    </div>
  );
}