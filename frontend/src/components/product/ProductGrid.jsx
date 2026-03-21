import ProductCard from './ProductCard';

export default function ProductGrid({
  products = [],
  showDistance = false,
  showFarm = true,
  columns = 4,
}) {
  const gridClass = {
    2: 'grid-cols-1 sm:grid-cols-2',
    3: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3',
    4: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
  };

  return (
    <div className={`grid ${gridClass[columns] || gridClass[4]} gap-6`}>
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          showDistance={showDistance}
          showFarm={showFarm}
        />
      ))}
    </div>
  );
}