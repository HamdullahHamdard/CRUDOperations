package com.hamdard.CRUDOperations.controllers;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.hamdard.CRUDOperations.models.Product;
import com.hamdard.CRUDOperations.models.ProductDto;
import com.hamdard.CRUDOperations.services.ProductRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {
    public static final String RED = "\033[0;31m"; 

    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"", "/"})
    public String showProducts(Model model){
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }


    @GetMapping("/create")
    public String showCreatePage(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result){
        if(productDto.getImageFile().isEmpty()){
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }
        if(result.hasErrors()){
            return "products/CreateProduct";
        }

        // TODO::  SAVE IMAGE FILE

        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime()+"_"+image.getOriginalFilename();
        try {
            String uploadDir = "public/images";
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            try(InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir, storageFileName), StandardCopyOption.REPLACE_EXISTING );
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println(RED + "Message: "+ e.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setDescription(productDto.getDescription());
        product.setImageFileName(storageFileName);
        product.setCreatedAt(createdAt);
        product.setPrice(productDto.getPrice());

        // save the products
        productRepository.save(product);

        
        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEdit(Model model, @RequestParam int id){

        try {
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());
            
            model.addAttribute("productDto", productDto);

        } catch (Exception e) {
            // TODO: handle exception
            System.err.println(RED+"Message: "+ e.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }
    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result){

        try {
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            if(result.hasErrors()){
                return "products/EditProduct";
            }
            if(!productDto.getImageFile().isEmpty()){
                String uploadDir = "public/images";
                Path oldPath = Paths.get(uploadDir+product.getImageFileName());
                try {
                    Files.delete(oldPath);
                } catch (Exception e) {
                    // TODO: handle exception
                    System.err.println(RED+"Message: "+e.getMessage());
                }
                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime()+"_"+image.getOriginalFilename();
                try(InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());

            productRepository.save(product);
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println(RED+"Message: "+e.getMessage());
        }
        return "redirect:/products";

    }

    @GetMapping("/delete")
    public String delete(
        @RequestParam int id
    ){
        try {
            Product product = productRepository.findById(id).get();
            
                String uploadDir = "public/images";
                Path oldPath = Paths.get(uploadDir+product.getImageFileName());
                try {
                    Files.delete(oldPath);
                } catch (Exception e) {
                    // TODO: handle exception
                    System.err.println(RED+"Message: "+e.getMessage());
                }
                productRepository.delete(product);
            
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println(RED+"Message: "+e.getMessage());
        }
        return "redirect:/products";

    }
}
