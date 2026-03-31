# language: pt

Funcionalidade: Reservar Agendamento
  Como cliente do salão
  Quero reservar um horário com um profissional
  Para garantir meu atendimento no momento desejado

  Contexto:
    Dado que existe um estabelecimento com profissional disponível
    E que existe um cliente cadastrado no sistema

  Cenário: Reserva com sucesso para horário disponível
    Quando o cliente solicita o agendamento para "2026-10-15T10:00:00"
    Então o sistema retorna status 201
    E o agendamento é persistido com status "PENDENTE"

  Cenário: Reserva rejeitada para horário já ocupado
    Dado que já existe uma reserva para o horário "2026-10-15T10:00:00"
    Quando o cliente solicita o agendamento para "2026-10-15T10:00:00"
    Então o sistema retorna status 422

  Cenário: Confirmação de agendamento pendente
    Dado que existe um agendamento pendente para o horário "2026-10-15T11:00:00"
    Quando o estabelecimento confirma o agendamento
    Então o status do agendamento é "CONFIRMADO"

  Cenário: Cancelamento de agendamento pendente
    Dado que existe um agendamento pendente para o horário "2026-10-15T14:00:00"
    Quando o cliente cancela o agendamento
    Então o status do agendamento é "CANCELADO"
