-- Select product with id B0002S30NE
SELECT product_id, pgroup, average_rating FROM Product WHERE Product.product_id = 'B0002S30NE';

-- Insert a new review for product B0002S30NE
INSERT INTO Review (review_id, customer_id, rating, description, product_id)
VALUES ('123456789', 'weisserstier', 5, 'Super geil', 'B0002S30NE');

-- Select product B0002S30NE again and view the updated average rating
SELECT product_id, pgroup, average_rating FROM Product WHERE product_id = 'B0002S30NE';

-- Delete the review with id 123456789 --> Repeat cycle
DELETE FROM Review WHERE review_id = '123456789';