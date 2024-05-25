-- Index PGroup
CREATE INDEX idx_getPGroup ON product (pgroup);

-- Index getProduct
CREATE INDEX idx_getProduct ON product (product_id);

-- Index getProducts
CREATE INDEX idx_getProducts ON product (title);

-- Index getCategoryTree
CREATE INDEX idx_getCategoryTree ON category (parent_category);

-- Index getProductsByCategoryPath
CREATE INDEX idx_getProductsByCategoryPath ON category (name, parent_category);

-- Index getTopProducts
CREATE INDEX idx_getTopProducts ON review (rating);

-- Index getSimilarCheaperProduct
CREATE INDEX idx_getSimilarCheaperProduct ON offer (price);

-- Index addNewReview
CREATE INDEX idx_addNewReview ON review (product_id, customer_id);

-- Index getTrolls
CREATE INDEX idx_getTrolls ON review (customer_id);

-- Index getOffers
CREATE INDEX idx_getOffers ON offer (product_id);