package com.recipe.controllers;

import com.recipe.domains.Ingredient;
import com.recipe.domains.Recipe;
import com.recipe.domains.User;
import com.recipe.repositories.RecipeRepository;
import com.recipe.repositories.UserRepository;
import com.recipe.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dough on 4/15/2017.
 */
@Controller
public class RecipeController {

    private final RecipeRepository recipeRepository;

    private final UserDetailsServiceImpl userDetailsService;

    private final UserRepository userRepository;

    @Autowired
    public RecipeController(RecipeRepository recipeRepository,
                            UserDetailsServiceImpl userDetailsService,
                            UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/recipes")
    public String displayRecipes(Model model) {
        User currentLoggedInUser = userDetailsService.getCurrentLoggedInUser();
        model.addAttribute("recipes", currentLoggedInUser.getRecipes() != null ? currentLoggedInUser.getRecipes() : new ArrayList<Recipe>());
        return "recipes";
    }

    @GetMapping("/recipe/{recipeId}")
    public String displayRecipe(Model model, @PathVariable("recipeId") UUID recipeId) {
        model.addAttribute("recipe", recipeRepository.findOne(recipeId));
        //Serve the /templates/recipe.html template.
        return "recipe";
    }

    @GetMapping("/create")
    public String serveForm(Model model) {

        Recipe recipe = new Recipe();
        model.addAttribute("recipe", recipe);
        return "create";
    }

    @PostMapping(value = "/create", params = {"addRow"})
    public String addRow(@ModelAttribute("recipe") Recipe recipe, final BindingResult bindingResult) {

        if (recipe.getIngredients() == null) {
            recipe.setIngredients(new ArrayList<>());
        }

        recipe.getIngredients().add(new Ingredient());
        return "create";
    }

    @PostMapping(value = "/create", params = {"removeRow"})
    public String removeRow(@ModelAttribute("recipe") Recipe recipe,
                            final BindingResult bindingResult,
                            HttpServletRequest request) {
        Integer rowId = Integer.valueOf(request.getParameter("removeRow"));
        recipe.getIngredients().remove(rowId.intValue());
        return "create";
    }

    @PostMapping("/create")
    public String saveRecipe(@ModelAttribute("recipe") @Valid Recipe recipe, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "create";
        }


        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.setRecipe(recipe);
            }
        }

        User currentLoggedInUser = userDetailsService.getCurrentLoggedInUser();

        recipe.setUser(currentLoggedInUser);
        currentLoggedInUser.getRecipes().add(recipe);
        userRepository.save(currentLoggedInUser);

        return "redirect:/recipes";
    }
}
