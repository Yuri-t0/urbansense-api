package br.com.urbanintel.infrastructure.adapter.in.web;

import br.com.urbanintel.tool.UrbanIntelTools;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/mcp")
@RequiredArgsConstructor
@Tag(name = "MCP", description = "Model Context Protocol — expõe ferramentas para clientes de IA externos")
public class McpController {

    private final UrbanIntelTools urbanTools;

    @GetMapping("/tools")
    @Operation(summary = "Lista ferramentas MCP disponíveis",
               description = "Retorna catálogo de ferramentas no formato MCP para integração com LLMs externos")
    public ResponseEntity<EntityModel<McpToolsResponse>> listTools() {

        var tools = List.of(
            new McpTool("getCurrentWeather",
                "Busca condições climáticas atuais de uma cidade brasileira",
                Map.of("city", "string", "latitude", "number", "longitude", "number")),
            new McpTool("getActiveAlerts",
                "Retorna alertas urbanos ativos (alagamento, UV, trânsito)",
                Map.of("city", "string")),
            new McpTool("getRainRiskForHour",
                "Analisa risco histórico de chuva para uma hora específica",
                Map.of("city", "string", "hour", "integer")),
            new McpTool("getMetroLinesRainImpact",
                "Retorna histórico de impacto de chuva nas linhas de metrô/trem de SP",
                Map.of())
        );

        var response = new McpToolsResponse("urban-intel-mcp", "1.0.0", tools);

        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(McpController.class).listTools()).withSelfRel(),
                linkTo(methodOn(McpController.class).callTool(null)).withRel("call-tool")
        ));
    }

    @PostMapping("/tools/call")
    @Operation(summary = "Executa uma ferramenta MCP",
               description = "Chama uma ferramenta pelo nome com os parâmetros fornecidos")
    public ResponseEntity<McpToolResult> callTool(@RequestBody McpToolCall call) {
        try {
            String result = switch (call.toolName()) {
                case "getCurrentWeather" -> urbanTools.getCurrentWeather(
                        (String) call.parameters().get("city"),
                        toDouble(call.parameters().get("latitude")),
                        toDouble(call.parameters().get("longitude")));
                case "getActiveAlerts" -> urbanTools.getActiveAlerts(
                        (String) call.parameters().get("city"));
                case "getRainRiskForHour" -> urbanTools.getRainRiskForHour(
                        (String) call.parameters().get("city"),
                        toInt(call.parameters().get("hour")));
                case "getMetroLinesRainImpact" -> urbanTools.getMetroLinesRainImpact();
                default -> "Ferramenta não encontrada: " + call.toolName();
            };
            return ResponseEntity.ok(new McpToolResult(call.toolName(), result, true));
        } catch (Exception e) {
            return ResponseEntity.ok(new McpToolResult(call.toolName(), "Erro: " + e.getMessage(), false));
        }
    }

    private double toDouble(Object val) {
        return val instanceof Number n ? n.doubleValue() : Double.parseDouble(val.toString());
    }

    private int toInt(Object val) {
        return val instanceof Number n ? n.intValue() : Integer.parseInt(val.toString());
    }

    record McpTool(String name, String description, Map<String, String> parameters) {}
    record McpToolsResponse(String serverName, String version, List<McpTool> tools) {}
    record McpToolCall(String toolName, Map<String, Object> parameters) {}
    record McpToolResult(String toolName, String result, boolean success) {}
}
