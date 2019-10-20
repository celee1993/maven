package com.leyou.api;

import com.leyou.domain.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BrandApi  {
    @GetMapping("/brand/{id}")
    Brand findBrandsById(@PathVariable("id") Long id);

    @GetMapping("/brand")
    List<Brand> findBrandsByIds(@RequestParam("ids") List<Long> ids);
}
