package com.fiap.agendamento_servico.infrastructure.config;

import com.fiap.agendamento_servico.application.usecases.AgendarServicoUseCase;
import com.fiap.agendamento_servico.application.usecases.ConsultarDisponibilidadeUseCase;
import com.fiap.agendamento_servico.application.usecases.ConsultarGradeEstabelecimentoUseCase;
import com.fiap.agendamento_servico.application.usecases.GerenciarAgendamentoUseCase;
import com.fiap.agendamento_servico.application.usecases.GerenciarClienteUseCase;
import com.fiap.agendamento_servico.application.usecases.GerenciarEstabelecimentoUseCase;
import com.fiap.agendamento_servico.application.usecases.GerenciarProfissionalUseCase;
import com.fiap.agendamento_servico.application.usecases.GerenciarServicoEstabelecimentoUseCase;
import com.fiap.agendamento_servico.application.usecases.RegistrarAvaliacaoUseCase;
import com.fiap.agendamento_servico.application.usecases.VisualizarAgendaProfissionalUseCase;
import com.fiap.agendamento_servico.application.ports.in.ConsultarGradeEstabelecimentoUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.GerenciarClienteUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.GerenciarServicoUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.AvaliacaoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.NotificacaoPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.services.CalculadoraDeMediaAvaliacao;
import com.fiap.agendamento_servico.domain.services.GeradorDeAgenda;
import com.fiap.agendamento_servico.domain.services.ValidadorAgendamento;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoUseCase {

        @Bean
        public VisualizarAgendaProfissionalUseCase visualizarAgendaProfissionalUseCase(
                ProfissionalRepositorioPort profissionalRepositorioPort,
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                AgendamentoRepositorioPort agendamentoRepositorioPort,
                GeradorDeAgenda geradorDeAgenda) 
        {
                return new VisualizarAgendaProfissionalUseCase(
                        profissionalRepositorioPort,
                        estabelecimentoRepositorioPort,
                        agendamentoRepositorioPort,
                        geradorDeAgenda);
        }

        @Bean
        public ConsultarGradeEstabelecimentoUseCasePort consultarGradeEstabelecimentoUseCase(
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                ProfissionalRepositorioPort profissionalRepositorioPort,
                AgendamentoRepositorioPort agendamentoRepositorioPort,
                ServicoRepositorioPort servicoRepositorioPort,
                GeradorDeAgenda geradorDeAgenda)
        {
                return new ConsultarGradeEstabelecimentoUseCase(
                        estabelecimentoRepositorioPort,
                        profissionalRepositorioPort,
                        agendamentoRepositorioPort,
                        servicoRepositorioPort,
                        geradorDeAgenda);
        }

        @Bean
        public ConsultarDisponibilidadeUseCase consultarDisponibilidadeUseCase(
                ProfissionalRepositorioPort profissionalRepositorioPort,
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                AgendamentoRepositorioPort agendamentoRepositorioPort,
                GeradorDeAgenda geradorDeAgenda)
        {
                return new ConsultarDisponibilidadeUseCase(
                        profissionalRepositorioPort,
                        estabelecimentoRepositorioPort,
                        agendamentoRepositorioPort,
                        geradorDeAgenda
                );
        }

        @Bean
        public AgendarServicoUseCase agendarServicoUseCase(
                ProfissionalRepositorioPort profissionalRepositorioPort,
                ServicoRepositorioPort servicoRepositorioPort,
                AgendamentoRepositorioPort agendamentoRepositorioPort,
                ClienteRepositorioPort clienteRepositorioPort,
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                ValidadorAgendamento validadorAgendamento) 
        {
                return new AgendarServicoUseCase(
                        profissionalRepositorioPort,
                        servicoRepositorioPort,
                        agendamentoRepositorioPort,
                        clienteRepositorioPort,
                        estabelecimentoRepositorioPort,
                        validadorAgendamento);
        }

        @Bean
        public GerenciarAgendamentoUseCase gerenciarAgendamentoUseCase(
                AgendamentoRepositorioPort agendamentoRepositorioPort,
                ServicoRepositorioPort servicoRepositorioPort,
                ProfissionalRepositorioPort profissionalRepositorioPort,
                ClienteRepositorioPort clienteRepositorioPort,
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                NotificacaoPort notificacaoPort,
                ValidadorAgendamento validadorAgendamento)
        {       
                return new GerenciarAgendamentoUseCase(
                        agendamentoRepositorioPort,
                        servicoRepositorioPort,
                        profissionalRepositorioPort,
                        clienteRepositorioPort,
                        estabelecimentoRepositorioPort,
                        notificacaoPort,
                        validadorAgendamento);
        }

        @Bean
        public GerenciarEstabelecimentoUseCase gerenciarEstabelecimentoUseCase(
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                ServicoRepositorioPort servicoRepositorioPort,
                ProfissionalRepositorioPort profissionalRepositorioPort,
                AgendamentoRepositorioPort agendamentoRepositorioPort,
                AvaliacaoRepositorioPort avaliacaoRepositorioPort) 
        {
                return new GerenciarEstabelecimentoUseCase(
                        estabelecimentoRepositorioPort,
                        servicoRepositorioPort,
                        profissionalRepositorioPort,
                        agendamentoRepositorioPort,
                        avaliacaoRepositorioPort);
        }

        @Bean
        public GerenciarServicoUseCasePort gerenciarServicoEstabelecimentoUseCase(
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                ServicoRepositorioPort servicoRepositorioPort,
                ProfissionalRepositorioPort profissionalRepositorioPort,
                AgendamentoRepositorioPort agendamentoRepositorioPort) 
        {
                return new GerenciarServicoEstabelecimentoUseCase(
                        estabelecimentoRepositorioPort,
                        servicoRepositorioPort,
                        profissionalRepositorioPort,
                        agendamentoRepositorioPort
        );
        }

        @Bean
        public GerenciarProfissionalUseCase gerenciarProfissionalUseCase(
                ProfissionalRepositorioPort profissionalRepositorioPort,
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                ServicoRepositorioPort servicoRepositorioPort,
                AgendamentoRepositorioPort agendamentoRepositorioPort) 
        {
                return new GerenciarProfissionalUseCase(
                        profissionalRepositorioPort,
                        estabelecimentoRepositorioPort,
                        servicoRepositorioPort,
                        agendamentoRepositorioPort
                );
        }

        @Bean
        public RegistrarAvaliacaoUseCase registrarAvaliacaoUseCase(
                AgendamentoRepositorioPort agendamentoRepositorioPort,
                AvaliacaoRepositorioPort avaliacaoRepositorioPort,
                ProfissionalRepositorioPort profissionalRepositorioPort,
                EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
                CalculadoraDeMediaAvaliacao calculadoraDeMediaAvaliacao) 
        {
                return new RegistrarAvaliacaoUseCase(
                        agendamentoRepositorioPort,
                        avaliacaoRepositorioPort,
                        profissionalRepositorioPort,
                        estabelecimentoRepositorioPort,
                        calculadoraDeMediaAvaliacao);
        }

        @Bean
        public GerenciarClienteUseCasePort gerenciarClienteUseCase(ClienteRepositorioPort clienteRepositorioPort) {
                return new GerenciarClienteUseCase(clienteRepositorioPort);
        }
}
