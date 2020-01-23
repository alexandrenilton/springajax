package com.learning.springbootajax.service;

import com.learning.springbootajax.domain.Promocao;
import com.learning.springbootajax.repository.PromocaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class PromocaoDataTablesService {
    private static final Logger log = LoggerFactory.getLogger(PromocaoDataTablesService.class);

    private String[] cols = {
            "id", "titulo", "site", "linkPromocao", "descricao", "linkImagem", "preco", "likes", "dtCadastro", "categoria"
    };

    public Map<String, Object> execute(PromocaoRepository repository, HttpServletRequest request) {
        int start = Integer.parseInt(request.getParameter("start"));
        int length = Integer.parseInt(request.getParameter("length"));
        int draw = Integer.parseInt(request.getParameter("draw"));

        int current = currentPage(start, length);

        String column = columnName(request);
        Sort.Direction direction = orderBy(request);
        String search = searchBy(request);

        Pageable pageable = PageRequest.of(current, length, direction, column);

        Page<Promocao> page = queryBy(search, repository, pageable);

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("draw", null);
        json.put("recordsTotal", page.getTotalElements());
        json.put("recordsFiltered", page.getTotalElements());
        json.put("data", page.getContent());

        return json;
    }

    private String searchBy(HttpServletRequest request) {
        return request.getParameter("search[value]").isEmpty()
                ? "" : request.getParameter("search[value]");
    }

    private Page<Promocao> queryBy(
            String search, PromocaoRepository repository, Pageable pageable) {
        if (search.isEmpty()) {
            return repository.findAll(pageable);
        }
        // testa se é um valor monetário, pq se for a pesquisa será por preço
        // tstado em https://regex101.com
        if (search.matches("^[0-9]+([.,][0-9]{2})?$" )) {
            search = search.replace(",", ".");
            return repository.findByPreco(new BigDecimal(search), pageable);
        }
        return repository.findByTituloOrSiteOrCategoria(search, pageable);
    }

    private Sort.Direction orderBy(HttpServletRequest request){
        String order = request.getParameter("order[0][dir]");
        Sort.Direction sort = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("desc")) {
            sort = Sort.Direction.DESC;
        }
        return sort;
    }

    private int currentPage(int start, int lenght) {
        //0     | 1      | 2
        //0-9   | 10-19  | 20-29
        return start / lenght;
    }

    private String columnName(HttpServletRequest request) {
        int iCol = Integer.parseInt(request.getParameter("order[0][column]"));
        return cols[iCol];
    }


}
