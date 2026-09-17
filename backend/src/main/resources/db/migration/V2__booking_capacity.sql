-- Uma linha por rota permite serializar reservas concorrentes entre instâncias da API.
CREATE TABLE flight_routes (id VARCHAR(6) PRIMARY KEY);
INSERT INTO flight_routes (id) VALUES ('FL-001'), ('FL-002'), ('FL-003'), ('FL-004');
CREATE INDEX idx_reservas_flight_status ON reservas(flight_id, status);
CREATE INDEX idx_reservas_hotel_arrival ON reservas(hotel_id, flight_chegada, status);
