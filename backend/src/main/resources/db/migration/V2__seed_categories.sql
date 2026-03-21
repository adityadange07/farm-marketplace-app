-- ═══ Seed Categories ═══
INSERT INTO categories (id, name, slug, sort_order) VALUES
                                                        (UUID(), 'Vegetables', 'vegetables', 1),
                                                        (UUID(), 'Fruits', 'fruits', 2),
                                                        (UUID(), 'Dairy & Eggs', 'dairy-eggs', 3),
                                                        (UUID(), 'Meat & Poultry', 'meat-poultry', 4),
                                                        (UUID(), 'Grains & Cereals', 'grains-cereals', 5),
                                                        (UUID(), 'Honey & Preserves', 'honey-preserves', 6),
                                                        (UUID(), 'Herbs & Spices', 'herbs-spices', 7),
                                                        (UUID(), 'Flowers & Plants', 'flowers-plants', 8);