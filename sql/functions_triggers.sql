-- https://www.postgresql.org/docs/current/plpgsql-trigger.html

CREATE OR REPLACE FUNCTION calculate_average_ratings_ontrigger()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        UPDATE Product
        SET average_rating = (SELECT AVG(rating)
                              FROM Review
                              WHERE product_id = NEW.product_id)
        WHERE product_id = NEW.product_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE Product
        SET average_rating = (SELECT AVG(rating)
                              FROM Review
                              WHERE product_id = OLD.product_id)
        WHERE product_id = OLD.product_id;
    END IF;
    RETURN NULL;
END ;
$$ LANGUAGE plpgsql;


CREATE TRIGGER update_average_rating_on_insert_or_delete_reviews
    AFTER INSERT OR DELETE OR UPDATE
    ON Review
    FOR EACH ROW
EXECUTE FUNCTION calculate_average_ratings_ontrigger();


CREATE OR REPLACE FUNCTION calculate_average_ratings()
    RETURNS void AS
$$
BEGIN
    UPDATE Product
    SET average_rating = (SELECT AVG(rating)
                          FROM (SELECT rating
                                FROM Review
                                WHERE Review.product_id = Product.product_id) AS subquery);
END;
$$ LANGUAGE plpgsql;