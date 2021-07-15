CREATE DATABASE Train_routes;
USE Train_routes;

CREATE TABLE Route 
(
  	ID_route INT NOT NULL AUTO_INCREMENT,
  	Number_route INT NULL,
    Departure_station CHAR(30) NULL,
    Arrival_station CHAR(30) NULL,
	PRIMARY KEY (ID_route)
)	ENGINE=InnoDB;

CREATE TABLE Stations 
(
  	ID_note INT NOT NULL AUTO_INCREMENT,
  	ID_route INT NOT NULL,
    Station_name CHAR(30) NULL,
	Departure_time TIME NULL,
	Parking_time TIME NULL,
	Arrival_time TIME NULL,
	Travel_time TIME NULL,
    PRIMARY KEY (ID_note),
    FOREIGN KEY (ID_route) REFERENCES Route (ID_route) ON DELETE CASCADE ON UPDATE CASCADE
)	ENGINE=InnoDB;

