package com.learning.springbootajax.web.controller;

import com.learning.springbootajax.config.BigDecimalEditor;
import com.learning.springbootajax.domain.Categoria;
import com.learning.springbootajax.domain.Promocao;
import com.learning.springbootajax.dto.PromocaoDTO;
import com.learning.springbootajax.repository.CategoriaRepository;
import com.learning.springbootajax.repository.PromocaoRepository;
import com.learning.springbootajax.service.PromocaoDataTablesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/promocao")
public class PromocaoController {

    private static Logger log = LoggerFactory.getLogger(PromocaoController.class);

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PromocaoRepository promocaoRepository;

    // se trata da variavel categorias no front (promo-add.html)
    @ModelAttribute("categorias")
    public List<Categoria> getCategorias() {
        return categoriaRepository.findAll();
    }

    @GetMapping("/add")
    public String abrirCadastro() {
        return "promo-add";
    }

    /** ================== DATA TABLES  ================== */
    @GetMapping("/table")
    public String showTable() {
        return "promo-datatables";
    }

    @GetMapping("/datatables/server")
    public ResponseEntity<?> datatables(HttpServletRequest request) {
        Map<String, Object> data = new PromocaoDataTablesService().execute(promocaoRepository, request);
        return ResponseEntity.ok(data);
    }

    /** delete do modal **/
    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deletePromo(@PathVariable("id") Long id){
        promocaoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }


    /** ================== EDIT  ================== */

    // pega os dados pra preencher o modal..
    @GetMapping("/edit/{id}")
    public ResponseEntity<?> preEdit(@PathVariable("id") Long id){
        Promocao promo = promocaoRepository.findById(id).get();
        return ResponseEntity.ok(promo);
    }

    // confirma edição feita no modal
    public ResponseEntity<?> editPromo(@Valid PromocaoDTO dto, BindingResult result) {
        if ( result.hasErrors() ) {
            Map<String, String> errors = getMapErrors(result);
            return ResponseEntity.unprocessableEntity().body(errors);
        }
        // nenhum erro. pode atualizar
        Promocao promo = promocaoRepository.findById(dto.getId()).get();
        // atualiza os campos com os novos dados..
        promo.setUpdateFields(dto);
        // atualiza a promo
        promocaoRepository.save(promo);

        return ResponseEntity.ok().build();
    }

    /** ================== ADD lIKES  ================== */
    @PostMapping("/like/{id}")
    public ResponseEntity<?> addLikes(@PathVariable("id") Long id) {
        promocaoRepository.updateSumLikes(id);
        int likes = promocaoRepository.findLikesById(id);
        return ResponseEntity.ok(likes);
    }

    /** ================== LIST ================== */
    @GetMapping("/list")
    public String listarOfertas(ModelMap model) {
        Sort sort = new Sort(Sort.Direction.DESC, "dtCadastro"); // sort
        PageRequest pageRequest = PageRequest.of(0, 8, sort); // 8 elemntos por pagina

        model.addAttribute("promocoes", promocaoRepository.findAll(pageRequest));
        return "promo-list";
    }

    /** ================== AUTOCOMPLETE ================== */
    @GetMapping("/site")
    public ResponseEntity<?> autoCompleteByTerm(@RequestParam("termo") String termo) {
        List<String> sites = promocaoRepository.findSitesByTermo(termo);
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/site/list")
    public String listBySite(@RequestParam("site") String site, ModelMap modelMap) {
        Sort sort = new Sort(Sort.Direction.DESC, "dtCadastro");
        PageRequest pageRequest = PageRequest.of(0, 8, sort);
        modelMap.addAttribute("promocoes", promocaoRepository.findBySite(site, pageRequest));
        return "promo-card";
    }

    // la no data está passando uma variavel chamada page..
    @GetMapping("/list/ajax")
    public String listCards(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "site", defaultValue = "") String site,
            ModelMap modelMap) {
        Sort sort = new Sort(Sort.Direction.DESC, "dtCadastro"); // sort
        PageRequest pageRequest = PageRequest.of(page, 8, sort); // 8 elemntos por pagina
        if (site.isEmpty()) {
            modelMap.addAttribute("promocoes", promocaoRepository.findAll(pageRequest));
        } else {
            modelMap.addAttribute("promocoes", promocaoRepository.findBySite(site, pageRequest));
        }
        return "promo-card";
    }
    /** ================== SAVE ================== */
    @PostMapping("/save")
    public ResponseEntity<?> salvarPromocao(
            @Valid Promocao promocao,
            BindingResult result ) {

        if ( result.hasErrors() ) {
            Map<String, String> errors = getMapErrors(result);
            return ResponseEntity.unprocessableEntity().body(errors);
        }

        log.info("Promocao {}", promocao.toString());

        promocao.setDtCadastro(LocalDateTime.now());
        promocaoRepository.save(promocao);
        return ResponseEntity.ok().build();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new BigDecimalEditor());
    }




    private Map<String, String> getMapErrors(BindingResult result) {
        if ( result.hasErrors() ) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return errors;
        }
        return null;
    }

}
