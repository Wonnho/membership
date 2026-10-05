-- Dummy members: generated IDs will be 1, 2, and 3
INSERT INTO member (
    email,
    password,
    email_verified,
    verification_attempts,
    verification_sends,
    role
) VALUES (
    'Grok@gmail.com',
    NULL,
    TRUE,
    0,
    0,
    'MEMBER'
);

INSERT INTO member (
    email,
    password,
    email_verified,
    verification_attempts,
    verification_sends,
    role
) VALUES (
    'Charles@spaceX.com',
    NULL,
    TRUE,
    0,
    0,
    'MEMBER'
);

INSERT INTO member (
    email,
    password,
    email_verified,
    verification_attempts,
    verification_sends,
    role
) VALUES (
    'Henry@nasa.com',
    NULL,
    TRUE,
    0,
    0,
    'MEMBER'
);

INSERT INTO coffee (coffee, price, image)
VALUES ('Americano', 4500, '/images/coffee/americano.jpg');

INSERT INTO coffee (coffee, price, image)
VALUES ('Latte', 5000, '/images/coffee/coffee-latte.jpg');

INSERT INTO coffee (coffee, price, image)
VALUES ('Cafe Mocha', 5500, '/images/coffee/cafe-mocha.jpg');

-- Three reviews for Americano (coffee_id = 1)
INSERT INTO review (coffee_id, member_id, review)
VALUES (1, 1, 'Strong, clean, and refreshing.');

INSERT INTO review (coffee_id, member_id, review)
VALUES (1, 2, 'Smooth taste with a pleasant aroma.');

INSERT INTO review (coffee_id, member_id, review)
VALUES (1, 3, 'A great coffee for starting the day.');

-- Three reviews for Latte (coffee_id = 2)
INSERT INTO review (coffee_id, member_id, review)
VALUES (2, 1, 'Creamy and well balanced.');

INSERT INTO review (coffee_id, member_id, review)
VALUES (2, 2, 'The espresso and milk work perfectly together.');

INSERT INTO review (coffee_id, member_id, review)
VALUES (2, 3, 'Soft, smooth, and easy to drink.');

-- Three reviews for Cafe Mocha (coffee_id = 3)
INSERT INTO review (coffee_id, member_id, review)
VALUES (3, 1, 'Rich chocolate flavor with a good coffee taste.');

INSERT INTO review (coffee_id, member_id, review)
VALUES (3, 2, 'Sweet and satisfying without being too heavy.');

INSERT INTO review (coffee_id, member_id, review)
VALUES (3, 3, 'A delicious choice for chocolate lovers.');