CREATE TABLE well_licences (
  id INTEGER PRIMARY KEY,
  uwi VARCHAR(255),
  licence_number VARCHAR(255),
  well_name VARCHAR(255)
);

CREATE TABLE well_doi (
  id INTEGER PRIMARY KEY,
  well_licence_id INTEGER,
  partner_name VARCHAR(255),
  partner_ba_code VARCHAR(255),
  working_interest FLOAT
)