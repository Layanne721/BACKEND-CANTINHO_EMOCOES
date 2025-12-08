package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.model.Avaliacao;
import com.cantinho_emocoes.model.TipoAvaliacao;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.AvaliacaoRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avaliacoes")

public class AvaliacaoController {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoController(AvaliacaoRepository avaliacaoRepository, UsuarioRepository usuarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/templates")
    public ResponseEntity<Map<String, Map<String, Object>>> getTemplatesAvaliacao() {
        Map<String, Map<String, Object>> templates = new HashMap<>();

        // --- EF: ESCUTA, FALA, PENSAMENTO E IMAGINAÇÃO (13 ITENS) ---
        Map<String, String> ef = new HashMap<>();
        ef.put("1", "RECONHECE QUANDO É CHAMADO POR SEU NOME E IDENTIFICA OS NOMES DE PESSOAS DO SEU CONVIVIO.");
        ef.put("2", "DEMONSTRA INTERESSE AO OUVIR LEITURAS, POEMAS, MÚSICAS, HISTÓRIAS LIDAS OU CONTADAS.");
        ef.put("3", "IDENTIFICA ELEMENTOS DAS ILUSTRAÇÕES DE HISTÓRIAS DOS LIVROS OU OUTROS.");
        ef.put("4", "IMITA AS VARIAÇÕES DE ENTONAÇÃO E GESTOS, EXPERIMENTANDO AS MÚLTIPLAS LINGUAGENS.");
        ef.put("5", "COMUNICA-SE COM OUTRAS PESSOAS USANDO MOVIMENTOS, GESTOS OU OUTRAS MANIFESTAÇÕES EXPRESSIVAS.");
        ef.put("6", "RECONHECE A ESCRITA DO SEU PRÓPRIO NOME.");
        ef.put("7", "RECONHECE SEMELHANÇAS E DIFERENÇAS ENTRE O SEU NOME E O DOS COLEGAS.");
        ef.put("8", "APRECIA A ESCRITA DE PEQUENAS HISTÓRIAS.");
        ef.put("9", "PERGUNTA E RESPONDE PERGUNTAS SOBRE FATOS DE HISTÓRIAS NARRADAS.");
        ef.put("10", "IDENTIFICA CENÁRIOS, PERSONAGENS E PRINCIPAIS ACONTECIMENTOS A PARTIR DE ILUSTRAÇÕES.");
        ef.put("11", "RELATA EXPERIÊNCIAS E FATOS ACONTECIDOS.");
        ef.put("12", "ESCREVE SEU NOME UTILIZANDO ESCRITA ESPONTÂNEA OU CONVENCIONAL.");
        ef.put("13", "FOLHEIA LIVROS, ESCOLHENDO AQUELES QUE MAIS GOSTAM, ORIENTANDO-SE POR TEMAS E ILUSTRAÇÕES.");
        templates.put("EF", Map.of("titulo", "Escuta, Fala, Pensamento e Imaginação (EF)", "questoes", ef));

        // --- EO: EU, O OUTRO, O NÓS (13 ITENS) ---
        Map<String, String> eo = new HashMap<>();
        eo.put("1", "INTERAGE COM CRIANÇAS DA MESMA E DE OUTRAS FAIXAS ETÁRIAS E COM ADULTOS.");
        eo.put("2", "COMUNICA NECESSIDADES, DESEJOS E EMOÇÕES.");
        eo.put("3", "COMPARTILHA, EXPLORA E ORGANIZA OS OBJETOS E ESPAÇOS COM CRIANÇAS E ADULTOS.");
        eo.put("4", "COMUNICA-SE COM OS COLEGAS E OS ADULTOS, BUSCANDO COMPREENDÊ-LOS E FAZENDO-SE COMPREENDER.");
        eo.put("5", "OBEDECE A REGRAS E NORMAS SOCIAIS, NA PARTICIPAÇÃO DE BRINCADEIRAS.");
        eo.put("6", "UTILIZA SUAS HABILIDADES COMUNICATIVAS, PARA RESOLVER CONFLITOS.");
        eo.put("7", "APRESENTA INDEPENDÊNCIA RECONHECENDO SUAS CONQUISTAS E LIMITAÇÕES.");
        eo.put("8", "COMUNICA SUAS IDEIAS E SENTIMENTOS.");
        eo.put("9", "EXPLORA OS ESPAÇOS DO COTIDIANO, COM ATITUDE DE CURIOSIDADE.");
        eo.put("10", "VALORIZA SUA IDENTIDADE PESSOAL E CULTURAL.");
        eo.put("11", "DEMONSTRA EMPATIA PELOS OUTROS, PERCEBENDO QUE AS PESSOAS TÊM DIFERENTES SENTIMENTOS.");
        eo.put("12", "DESCOBRE SUAS POSSIBILIDADES E OS LIMITES DE SEU CORPO NAS BRINCADEIRAS.");
        eo.put("13", "AMPLIA AS RELAÇÕES INTERPESSOAIS DESENVOLVENDO ATITUDES DE PARTICIPAÇÃO E COOPERAÇÃO.");
        templates.put("EO", Map.of("titulo", "Eu, o Outro, o Nós (EO)", "questoes", eo));

        // --- TS: TRAÇOS, SONS, CORES E FORMAS (13 ITENS) ---
        Map<String, String> ts = new HashMap<>();
        ts.put("1", "EXPLORA SONS PRODUZIDOS COM O PRÓPRIO CORPO E COM OBJETOS DO AMBIENTE.");
        ts.put("2", "TRAÇA MARCAS GRÁFICAS EM DIFERENTES SUPORTES, FAZENDO O USO DE DIFERENTES RECURSOS.");
        ts.put("3", "PERCEBE A INTENSIDADE DOS SONS E DOS RITMOS, MOVIMENTANDO-SE DE ACORDO COM A MELODIA.");
        ts.put("4", "UTILIZA MATERIAIS VARIADOS COM POSSIBILIDADE DE MANIPULAÇÃO, EXPLORANDO CORES, TEXTURAS.");
        ts.put("5", "UTILIZA DIFERENTES ESTRATÉGIAS E LINGUAGENS PARA EXPRESSAR-SE.");
        ts.put("6", "EXPRESSA LIVREMENTE EMOÇÕES, NECESSIDADES E IDEIAS ATRAVÉS DE SUAS PRODUÇÕES ARTÍSTICAS.");
        ts.put("7", "VALORIZA AS PRODUÇÕES ARTÍSTICAS INDIVIDUAIS E COLETIVAS.");
        ts.put("8", "RECONHECE AS QUALIDADES DO SOM (DURAÇÃO, ALTURA, INTENSIDADE, TIMBRE).");
        ts.put("9", "PERCEBE A INTENSIDADE DOS SONS E OS RITMOS DAS MELODIAS ECOADAS PELO PRÓPRIO CORPO.");
        ts.put("10", "DESENVOLVE HABILIDADES DE APRECIAÇÃO E LEITURA DE IMAGENS, DESENHOS, ESCULTURAS.");
        ts.put("11", "RESPEITA AS DIFERENTES CULTURAS E IDENTIDADES.");
        ts.put("12", "PARTICIPA DE DECISÕES E AÇÕES RELATIVAS À ORGANIZAÇÃO DO AMBIENTE.");
        ts.put("13", "RECONHECE E VALORIZA O SEU PERTENCIMENTO ÉTNICO-RACIAL, DE GÊNERO E DE CRENÇA.");
        templates.put("TS", Map.of("titulo", "Traços, Sons, Cores e Formas (TS)", "questoes", ts));

        // --- ET: ESPAÇOS, TEMPOS, QUANTIDADES (13 ITENS) ---
        Map<String, String> et = new HashMap<>();
        et.put("1", "RECONHECE E IDENTIFICA ODORES, CORES, SABORES, TEMPERATURAS, CONSISTÊNCIA.");
        et.put("2", "VIVENCIA DIFERENTES RITMOS, VELOCIDADES E FLUXOS NAS INTERAÇÕES E BRINCADEIRAS.");
        et.put("3", "EXPERIMENTA LIVREMENTE AS DIVERSAS FORMAS DE DESLOCAMENTO NO ESPAÇO.");
        et.put("4", "EXPLORA O AMBIENTE INTERNO ESCOLAR POR MEIO DE PASSEIOS.");
        et.put("5", "OBSERVA, RELATA E DESCREVE INCIDENTES DO COTIDIANO.");
        et.put("6", "CLASSIFICA OBJETOS, A PARTIR DE DETERMINADOS ATRIBUTOS (TAMANHO, MASSA, COR).");
        et.put("7", "CONTA ORALMENTE OBJETOS PESSOAIS, LIVROS, ENTRE OUTROS.");
        et.put("8", "REGISTRA QUANTIDADES EM DIFERENTES FORMAS (NÚMEROS, GRÁFICOS, OBJETOS).");
        et.put("9", "ESTABELECE RELAÇÕES DE COMPARAÇÃO, OBSERVANDO SUAS PROPRIEDADES.");
        et.put("10", "CLASSIFICA OBJETOS E FIGURAS DE ACORDO COM SUAS SEMELHANÇAS E DIFERENÇAS.");
        et.put("11", "RECONHECE, RELATA E CONSTRÓI SUA LINHA DO TEMPO.");
        et.put("12", "RELACIONA NÚMEROS ÀS SUAS RESPECTIVAS QUANTIDADES.");
        et.put("13", "REGISTRA QUANTIDADE, COM ESCRITA NUMÉRICA, A PARTIR DO USO SOCIAL DO NÚMERO.");
        templates.put("ET", Map.of("titulo", "Espaços, Tempos, Quantidades (ET)", "questoes", et));

        return ResponseEntity.ok(templates);
    }

    @PostMapping
    public ResponseEntity<?> salvarAvaliacao(@RequestHeader("x-child-id") Long childId, @RequestBody Avaliacao avaliacao) {
        Usuario aluno = usuarioRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        
        // CORREÇÃO: Usa List para tratar duplicatas com segurança
        List<Avaliacao> existentes = avaliacaoRepository.findByAlunoIdAndTipoAndUnidade(
            childId, avaliacao.getTipo(), avaliacao.getUnidade()
        );

        Avaliacao aSalvar;
        if (!existentes.isEmpty()) {
            // Se já existir (ou tiver duplicatas), pega a primeira e atualiza
            aSalvar = existentes.get(0);
            aSalvar.setRespostas(avaliacao.getRespostas());
            aSalvar.setDataAvaliacao(LocalDateTime.now());
            
            // Opcional: Limpar duplicatas extras se existirem (para limpar o banco)
            if (existentes.size() > 1) {
                for(int i = 1; i < existentes.size(); i++) {
                    avaliacaoRepository.delete(existentes.get(i));
                }
            }
        } else {
            aSalvar = avaliacao;
            aSalvar.setAluno(aluno);
            aSalvar.setDataAvaliacao(LocalDateTime.now());
        }
        
        avaliacaoRepository.save(aSalvar);
        return ResponseEntity.ok(Map.of("message", "Avaliação salva com sucesso!"));
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarAvaliacao(
            @RequestHeader("x-child-id") Long childId,
            @RequestParam("tipo") TipoAvaliacao tipo,
            @RequestParam("unidade") String unidade
    ) {
        // CORREÇÃO: Usa List para evitar erro de NonUniqueResultException
        List<Avaliacao> existentes = avaliacaoRepository.findByAlunoIdAndTipoAndUnidade(childId, tipo, unidade);
        
        if (!existentes.isEmpty()) {
            return ResponseEntity.ok(existentes.get(0));
        } else {
            return ResponseEntity.ok(Map.of("respostas", new HashMap<>())); 
        }
    }

    @GetMapping("/aluno/{childId}")
    public ResponseEntity<List<Avaliacao>> listarAvaliacoes(@PathVariable Long childId) {
        return ResponseEntity.ok(avaliacaoRepository.findByAlunoIdOrderByDataAvaliacaoDesc(childId));
    }
}