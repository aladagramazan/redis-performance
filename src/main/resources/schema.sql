DROP TABLE IF EXISTS product_2;
CREATE TABLE product_2(
                        id serial PRIMARY KEY,
                        description VARCHAR (500),
                        price numeric (10,2) NOT NULL
);