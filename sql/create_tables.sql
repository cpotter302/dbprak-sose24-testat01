CREATE TABLE Shop
(
    shop_id serial      NOT NULL,
    name    varchar(50) NOT NULL,
    address varchar(50) NOT NULL,
    CONSTRAINT "Shop_pkey" PRIMARY KEY (shop_id)
);

CREATE TABLE Product
(
    product_id     varchar(10) NOT NULL,
    title          varchar     NOT NULL,
    salesrank      integer,
    image          varchar,
    pgroup         varchar(5)  NOT NULL,
    ean            varchar(13),
    average_rating float8,
    CONSTRAINT "Product_pkey" PRIMARY KEY (product_id)
);

CREATE TABLE Product_Similars
(
    product_id      varchar(10) NOT NULL,
    similar_product varchar(10) NOT NULL,
    CONSTRAINT "Product_Similars_pkey" PRIMARY KEY (product_id, similar_product),
    CONSTRAINT "Product_Similars_product_id_fkey" FOREIGN KEY (product_id) REFERENCES Product (product_id) ON DELETE CASCADE,
    CONSTRAINT "Product_Similars_similar_product_fkey" FOREIGN KEY (similar_product) REFERENCES Product (product_id) ON DELETE CASCADE
);

CREATE TABLE Person
(
    person_id serial       NOT NULL,
    name      varchar(150) NOT NULL,
    CONSTRAINT "Person_pkey" PRIMARY KEY (person_id)
);

CREATE TABLE Book
(
    book_id     serial      NOT NULL,
    pageamount  integer     NOT NULL,
    publishdate date        NOT NULL,
    isbn        varchar(10) NOT NULL,
    publisher   varchar(50) NOT NULL,
    product_id  varchar(10) NOT NULL,
    CONSTRAINT "Book_pkey" PRIMARY KEY (book_id),
    CONSTRAINT "Book_product_id_fkey" FOREIGN KEY (product_id) REFERENCES Product (product_id) ON DELETE CASCADE
);

CREATE TABLE Book_Person
(
    book_id   integer NOT NULL,
    person_id integer NOT NULL,
    CONSTRAINT "Book_Person_pkey" PRIMARY KEY (book_id, person_id),
    CONSTRAINT "Book_Person_book_id_fkey" FOREIGN KEY (book_id) REFERENCES Book (book_id) ON DELETE CASCADE,
    CONSTRAINT "Book_Person_person_id_fkey" FOREIGN KEY (person_id) REFERENCES Person (person_id) ON DELETE CASCADE
);

CREATE TABLE CD
(
    cd_id       serial      NOT NULL,
    product_id  varchar(10) NOT NULL,
    label       varchar(50) NOT NULL,
    publishdate date        NOT NULL,
    CONSTRAINT "CD_pkey" PRIMARY KEY (cd_id),
    CONSTRAINT "CD_product_id_fkey" FOREIGN KEY (product_id) REFERENCES Product (product_id) ON DELETE CASCADE
);

CREATE TABLE CD_Person
(
    cd_id     integer     NOT NULL,
    person_id integer     NOT NULL,
    role      varchar(50) NOT NULL,
    CONSTRAINT "CD_Person_pkey" PRIMARY KEY (cd_id, person_id, role),
    CONSTRAINT "CD_Person_cd_id_fkey" FOREIGN KEY (cd_id) REFERENCES CD (cd_id) ON DELETE CASCADE,
    CONSTRAINT "CD_Person_person_id_fkey" FOREIGN KEY (person_id) REFERENCES Person (person_id) ON DELETE CASCADE
);

CREATE TABLE Title
(
    title_id serial       NOT NULL,
    name     varchar(250) NOT NULL,
    cd_id    integer      NOT NULL,
    CONSTRAINT "Title_pkey" PRIMARY KEY (title_id),
    CONSTRAINT "Title_cd_id_fkey" FOREIGN KEY (cd_id) REFERENCES CD (cd_id) ON DELETE CASCADE
);

CREATE TABLE DVD
(
    dvd_id      serial       NOT NULL,
    format      varchar(100) NOT NULL,
    runningtime integer      NOT NULL,
    region_code varchar(1)   NOT NULL,
    product_id  varchar(10)  NOT NULL,
    CONSTRAINT "DVD_pkey" PRIMARY KEY (dvd_id),
    CONSTRAINT "DVD_product_id_fkey" FOREIGN KEY (product_id) REFERENCES Product (product_id) ON DELETE CASCADE
);

CREATE TABLE DVD_Person
(
    dvd_id    integer     NOT NULL,
    person_id integer     NOT NULL,
    role      varchar(50) NOT NULL,
    CONSTRAINT "DVD_Person_pkey" PRIMARY KEY (dvd_id, person_id, role),
    CONSTRAINT "DVD_Person_dvd_id_fkey" FOREIGN KEY (dvd_id) REFERENCES DVD (dvd_id) ON DELETE CASCADE,
    CONSTRAINT "DVD_Person_person_id_fkey" FOREIGN KEY (person_id) REFERENCES Person (person_id) ON DELETE CASCADE
);

CREATE TABLE Customer
(
    customer_id         varchar(30) NOT NULL,
    bank_account_number varchar(22),
    address             varchar(50),
    CONSTRAINT "Customer_pkey" PRIMARY KEY (customer_id)
);

CREATE TABLE Review
(
    review_id   serial      NOT NULL,
    customer_id varchar(30) NOT NULL,
    rating      integer     NOT NULL,
    description text        NOT NULL,
    product_id  varchar(10) NOT NULL,
    CONSTRAINT "Review_pkey" PRIMARY KEY (review_id),
    CONSTRAINT "Review_rating_check" CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT "Review_customer_id_fkey" FOREIGN KEY (customer_id) REFERENCES Customer (customer_id) ON DELETE CASCADE,
    CONSTRAINT "Review_product_id_fkey" FOREIGN KEY (product_id) REFERENCES Product (product_id) ON DELETE CASCADE
);

CREATE TABLE Offer
(
    offer_id   serial      NOT NULL,
    price      float8,
    condition  varchar(20) NOT NULL,
    shop_id    integer     NOT NULL,
    product_id varchar(10) NOT NULL,
    CONSTRAINT "Offer_pkey" PRIMARY KEY (offer_id),
    CONSTRAINT unique_offer UNIQUE (price, condition, shop_id, product_id),
    CONSTRAINT "Offer_shop_id_fkey" FOREIGN KEY (shop_id) REFERENCES Shop (shop_id) ON DELETE CASCADE,
    CONSTRAINT "Offer_product_id_fkey" FOREIGN KEY (product_id) REFERENCES Product (product_id) ON DELETE CASCADE
);

CREATE TABLE Purchase
(
    purchase_id serial      NOT NULL,
    customer_id varchar(30) NOT NULL,
    offer_id    integer     NOT NULL,
    timestamp   timestamp   NOT NULL,
    CONSTRAINT "Purchase_pkey" PRIMARY KEY (purchase_id),
    CONSTRAINT "Purchase_offer_id_fkey" FOREIGN KEY (offer_id) REFERENCES Offer (offer_id) ON DELETE CASCADE,
    CONSTRAINT "Purchase_customer_id_fkey" FOREIGN KEY (customer_id) REFERENCES Customer (customer_id) ON DELETE CASCADE
);

CREATE TABLE Category
(
    category_id     serial       NOT NULL,
    name            varchar(200) NOT NULL,
    parent_category integer,
    CONSTRAINT "Category_pkey" PRIMARY KEY (category_id),
    CONSTRAINT "Category_unique_name_parent_category_id" UNIQUE (name, parent_category)
);

CREATE TABLE Product_Category
(
    product_id  varchar(10) NOT NULL,
    category_id integer     NOT NULL,
    CONSTRAINT "Product_Category_pkey" PRIMARY KEY (product_id, category_id),
    CONSTRAINT "Product_Category_category_id_fkey" FOREIGN KEY (category_id) REFERENCES Category (category_id) ON DELETE CASCADE,
    CONSTRAINT "Product_Category_product_id_fkey" FOREIGN KEY (product_id) REFERENCES Product (product_id) ON DELETE CASCADE
);


