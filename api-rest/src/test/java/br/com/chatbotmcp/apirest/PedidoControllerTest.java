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
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void buscarPedidoExistenteRetorna200ComDadosDoPedido() throws Exception {
        mockMvc.perform(get("/pedidos/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.clienteId").value(123))
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void buscarPedidoInexistenteRetorna404ComCorpoDeErro() throws Exception {
        mockMvc.perform(get("/pedidos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Pedido 999999 nao encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
