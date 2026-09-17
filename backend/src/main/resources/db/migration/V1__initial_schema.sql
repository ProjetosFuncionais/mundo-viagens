CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CLIENTE', 'ATENDENTE', 'GERENTE', 'DONA')),
    bloqueado BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE hoteis (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    cidade VARCHAR(120) NOT NULL,
    preco_diaria NUMERIC(12,2) NOT NULL CHECK (preco_diaria > 0),
    quartos_totais INTEGER NOT NULL CHECK (quartos_totais > 0)
);
CREATE TABLE reservas (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id),
    flight_id VARCHAR(40) NOT NULL,
    hotel_id UUID REFERENCES hoteis(id),
    flight_origem VARCHAR(255) NOT NULL,
    flight_destino VARCHAR(255) NOT NULL,
    flight_partida TIMESTAMP WITH TIME ZONE NOT NULL,
    flight_chegada TIMESTAMP WITH TIME ZONE NOT NULL,
    flight_preco NUMERIC(12,2) NOT NULL,
    flight_assentos INTEGER NOT NULL,
    hotel_nome VARCHAR(255),
    hotel_cidade VARCHAR(255),
    hotel_preco_diaria NUMERIC(12,2),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('cartao', 'boleto')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('pendente_pagamento', 'confirmada', 'cancelada')),
    total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    multa NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (multa >= 0),
    ticket_id VARCHAR(50)
);
CREATE TABLE cancelamentos (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id),
    reserva_id UUID NOT NULL UNIQUE REFERENCES reservas(id),
    data_cancelamento TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_reservas_user_created ON reservas(user_id, created_at);
CREATE INDEX idx_reservas_hotel ON reservas(hotel_id);
CREATE INDEX idx_cancelamentos_user_date ON cancelamentos(user_id, data_cancelamento);
