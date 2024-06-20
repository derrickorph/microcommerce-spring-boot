package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.dao.ProductDao;
import com.ecommerce.microcommerce.web.exceptions.ProductNotFoundException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
public class ProductController {
    @Autowired
    private final ProductDao productDao;

    public ProductController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public MappingJacksonValue productsList() {
        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("dynamicFilter", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;

    }
    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable int id) {
        Product product = productDao.findById(id);
        if(product==null) throw new ProductNotFoundException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");
        return product;
    }
    @DeleteMapping (value = "/products/{id}")
    public void deleteProduct(@PathVariable int id) {
        productDao.deleteById(id);
    }

    @PutMapping (value = "/products")
    public void updateProduct(@RequestBody Product product)
    {
        productDao.save(product);
    }
    @PostMapping(value = "/products")
    public ResponseEntity<Void> addProduct(@Valid @RequestBody Product product) {
        Product productAdded = productDao.save(product);
        if (Objects.isNull(productAdded)) {
            return ResponseEntity.noContent().build();
        }
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
    @GetMapping(value = "/products/price-greater-than/{prixLimit}")
    public List<Product> findByPrixGreaterThan(@PathVariable int prixLimit)
    {
        return productDao.findByPrixGreaterThan(prixLimit);
    }
}
