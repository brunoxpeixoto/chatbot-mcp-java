package br.com.chatbotmcp.apirest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void buscarClienteExistenteRetorna200ComDadosDoCliente() throws Exception {
        mockMvc.perform(get("/clientes/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria.silva@example.com"));
    }

    @Test
    void buscarClienteInexistenteRetorna404ComCorpoDeErro() throws Exception {
        mockMvc.perform(get("/clientes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente 999999 nao encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPedidosDoClienteRetornaListaOrdenadaDaMaisRecenteParaAMaisAntiga() throws Exception {
        mockMvc.perform(get("/clientes/123/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1003))
                .andExpect(jsonPath("$[1].id").value(1002))
                .andExpect(jsonPath("$[2].id").value(1001));
    }

    @Test
    void listarPedidosDeClienteInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/clientes/999999/pedidos"))
                .andExpect(status().isNotFound());
    }
}
