package com.rut.booking.controllers;

import com.rut.booking.security.CustomUserDetails;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/home";
    }

    @GetMapping("/contacts")
    public String contacts(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/contacts";
    }

    @GetMapping("/licenses")
    public String licenses(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/licenses";
    }

    @GetMapping("/policies")
    public String policies(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/policies";
    }

    @GetMapping("/faq")
    public String faq(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "pages/faq";
    }

    @GetMapping("/error/403")
    public String accessDenied(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }
        return "error/403";
    }

    @GetMapping("/campus-plan")
    public String campusPlan(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("user", userDetails);
        }

        // Сканируем SVG файлы из директории campus-plans
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:static/campus-plans/*.svg");

            Map<Integer, Set<Integer>> campusData = new TreeMap<>();
            List<Map<String, Object>> plans = new ArrayList<>();

            Pattern pattern = Pattern.compile("(\\d+)-(\\d+)\\.svg");

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    Matcher matcher = pattern.matcher(filename);
                    if (matcher.matches()) {
                        int building = Integer.parseInt(matcher.group(1));
                        int floor = Integer.parseInt(matcher.group(2));

                        campusData.computeIfAbsent(building, k -> new TreeSet<>()).add(floor);

                        Map<String, Object> plan = new HashMap<>();
                        plan.put("building", building);
                        plan.put("floor", floor);
                        plan.put("filename", filename);
                        plan.put("path", "/campus-plans/" + filename);
                        plans.add(plan);
                    }
                }
            }

            // Сортируем планы по корпусу и этажу
            plans.sort((p1, p2) -> {
                int buildingCompare = Integer.compare((Integer) p1.get("building"), (Integer) p2.get("building"));
                if (buildingCompare != 0) {
                    return buildingCompare;
                }
                return Integer.compare((Integer) p1.get("floor"), (Integer) p2.get("floor"));
            });

            List<Integer> buildings = new ArrayList<>(campusData.keySet());

            model.addAttribute("plans", plans);
            model.addAttribute("buildings", buildings);
            model.addAttribute("campusData", campusData);

        } catch (IOException e) {
            model.addAttribute("plans", new ArrayList<>());
            model.addAttribute("buildings", new ArrayList<>());
            model.addAttribute("campusData", new HashMap<>());
        }

        return "pages/campus-plan";
    }
}
