package com.example.demo.Controller;

import com.example.demo.Entities.Category;
import com.example.demo.Models.CategoryModel;
import com.example.demo.Service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("admin/categories")
public class CategoryController {
    @Autowired
    ICategoryService categoryService;

    // Hiển thị form thêm mới
    @GetMapping("add")
    public String add(ModelMap model) {
        CategoryModel cateModel = new CategoryModel();
        cateModel.setEdit(false); // đang ở chế độ thêm mới
        model.addAttribute("category", cateModel);
        return "admin/categories/AddOrEdit";
    }

    // Lưu hoặc cập nhật
//    @PostMapping("saveOrUpdate")
//    public ModelAndView saveOrUpdate(ModelMap model,
//                                     @Valid @ModelAttribute("category") CategoryModel cateModel,
//                                     BindingResult result) {
//        if (result.hasErrors()) {
//            return new ModelAndView("admin/categories/addOrEdit");
//        }
//        Category entity = new Category();
//        BeanUtils.copyProperties(cateModel, entity);
//
//        categoryService.save(entity);
//
//        String message = cateModel.isEdit() ? "Category is Edited!" : "Category is Saved!";
//        model.addAttribute("message", message);
//
//        return new ModelAndView("forward:/admin/categories/searchpaginated", model);
//    }
    @PostMapping("saveOrUpdate")
    public String saveOrUpdate(@Valid @ModelAttribute("category") CategoryModel cateModel,
                               BindingResult result,
                               RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            return "admin/categories/AddOrEdit";
        }
        Category entity = new Category();
        BeanUtils.copyProperties(cateModel, entity);
        categoryService.save(entity);
        redirectAttrs.addFlashAttribute("message", cateModel.isEdit() ? "Category is Edited!" : "Category is Saved!");
        return "redirect:/admin/categories/searchpaginated";
    }

    // Xem danh sách (không phân trang)
    @RequestMapping("")
    public String list(ModelMap model) {
        List<Category> list = categoryService.findAll();
        model.addAttribute("categories", list);
        return "admin/categories/list";
    }
    @GetMapping("view/{categoryId}")
    public String view(@PathVariable Long categoryId, ModelMap model) {
        Optional<Category> opt = categoryService.findById(categoryId);
        if (opt.isEmpty()) {
            model.addAttribute("message", "Category not found");
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", opt.get());
        return "admin/categories/view"; // tạo file view.html tương ứng
    }
    // Tìm kiếm (không phân trang)
    @GetMapping("search")
    public String search(ModelMap model,
                         @RequestParam(name = "name", required = false) String name) {
        List<Category> list;
        if (StringUtils.hasText(name)) {
            list = categoryService.findByCategoryNameContaining(name);
        } else {
            list = categoryService.findAll();
        }
        model.addAttribute("categories", list);
        return "admin/categories/search";
    }

    // Tìm kiếm + phân trang (đã viết lại gọn gàng)
    @RequestMapping("searchpaginated")
    public String searchPaginated(ModelMap model,
                                  @RequestParam(name = "name", required = false) String name,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(3);

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by("categoryName"));
        Page<Category> resultPage;

        if (StringUtils.hasText(name)) {
            resultPage = categoryService.findByCategoryNameContaining(name, pageable);
            model.addAttribute("name", name);
        } else {
            resultPage = categoryService.findAll(pageable);
        }

        int totalPages = resultPage.getTotalPages();
        if (totalPages > 0) {
            int start = Math.max(1, currentPage - 2);
            int end = Math.min(currentPage + 2, totalPages);
            if (currentPage > totalPages && totalPages > 0) {
                return "redirect:/admin/categories/searchpaginated?page=" + totalPages + "&size=" + pageSize + (StringUtils.hasText(name)? "&name="+name:""); // Gioi han so trang toi da neu nhap so trang lon qua muc
            }
            List<Integer> pageNumbers = IntStream.rangeClosed(start, end)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("categoryPage", resultPage);

        return "admin/categories/searchpaginated";
    }

    // Chỉnh sửa
    @GetMapping("edit/{categoryId}")
    public ModelAndView edit(ModelMap model,
                             @PathVariable("categoryId") Long categoryId) {
        Optional<Category> optCategory = categoryService.findById(categoryId);
        CategoryModel cateModel = new CategoryModel();

        if (optCategory.isPresent()) {
            Category entity = optCategory.get();
            BeanUtils.copyProperties(entity, cateModel);
            cateModel.setEdit(true);
            model.addAttribute("category", cateModel);
            return new ModelAndView("admin/categories/AddOrEdit", model);
        }

        model.addAttribute("message", "Category is not existed!");
        return new ModelAndView("forward:/admin/categories", model);
    }

    // Xoá
    @GetMapping("delete/{categoryId}")
    public ModelAndView delete(ModelMap model,
                               @PathVariable("categoryId") Long categoryId) {
        categoryService.deleteById(categoryId);
        model.addAttribute("message", "Category is deleted!");
        return new ModelAndView("forward:/admin/categories/searchpaginated", model);
    }
    @PostMapping("delete/{categoryId}")
    public String delete(RedirectAttributes redirectAttrs, @PathVariable Long categoryId) {
        categoryService.deleteById(categoryId);
        redirectAttrs.addFlashAttribute("message", "Category is deleted!");
        return "redirect:/admin/categories/searchpaginated";
    }
    @GetMapping("/ajax")
    public String showAjaxPage() {
        return "admin/categories/categories-ajax";
    }

}
