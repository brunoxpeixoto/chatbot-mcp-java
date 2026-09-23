INSERT INTO cliente (id, nome, email) VALUES (123, 'Maria Silva', 'maria.silva@example.com');
INSERT INTO cliente (id, nome, email) VALUES (456, 'Joao Pereira', 'joao.pereira@example.com');

INSERT INTO pedido (id, cliente_id, data, valor, status) VALUES (1001, 123, '2025-08-20', 150.00, 'ENTREGUE');
INSERT INTO pedido (id, cliente_id, data, valor, status) VALUES (1002, 123, '2025-09-05', 89.90, 'ENVIADO');
INSERT INTO pedido (id, cliente_id, data, valor, status) VALUES (1003, 123, '2025-09-18', 320.50, 'PAGO');

INSERT INTO pedido (id, cliente_id, data, valor, status) VALUES (2001, 456, '2025-09-10', 45.00, 'PENDENTE');
