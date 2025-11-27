INSERT IGNORE INTO region (id_region, nom_region) VALUES (13, 'Región Metropolitana de Santiago');
INSERT IGNORE INTO region (id_region, nom_region) VALUES (5, 'Región de Valparaíso');
INSERT IGNORE INTO region (id_region, nom_region) VALUES (7, 'Región del Maule');

INSERT IGNORE INTO comuna (id_comuna, nom_comuna, id_region) VALUES (13101, 'Santiago', 13);
INSERT IGNORE INTO comuna (id_comuna, nom_comuna, id_region) VALUES (13114, 'La Florida', 13);
INSERT IGNORE INTO comuna (id_comuna, nom_comuna, id_region) VALUES (13123, 'Maipú', 13);

INSERT IGNORE INTO comuna (id_comuna, nom_comuna, id_region) VALUES (5109, 'Viña del Mar', 5);
INSERT IGNORE INTO comuna (id_comuna, nom_comuna, id_region) VALUES (5101, 'Valparaíso', 5);

INSERT IGNORE INTO comuna (id_comuna, nom_comuna, id_region) VALUES (7101, 'Talca', 7);
INSERT IGNORE INTO comuna (id_comuna, nom_comuna, id_region) VALUES (7102, 'Constitución', 7);
