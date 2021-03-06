package com.example.springApp.controller;

import com.example.springApp.domain.*;
import com.example.springApp.repos.ActivityUsersCounter;
import com.example.springApp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserActivityTimeService userActivityTimeService;

    @Autowired
    private ActivityUsersCounterService activityUsersCounterService;


    @GetMapping("{user}")
    public String userActivityPage(@PathVariable User user,
                                   Model model){
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        model.addAttribute("activities", activityService.findAll());
        model.addAttribute("usersActivities", userActivityService.findByUserId(user.getId()));
        return "activity";
    }

    @GetMapping
    public String activityList(Model model,
                               @PageableDefault Pageable pageable
    ) {
        model.addAttribute("activities", activityUsersCounterService.countActivityUsers(pageable));
        model.addAttribute("url", "/activity");
        model.addAttribute("currentOrderDirection", "ASC");
        model.addAttribute("categories", categoryService.findAll());
        return "activity";
    }

    @GetMapping("/sort")
    public String activityList(Model model,
                               @RequestParam(required = false) String orderField,
                               @RequestParam(required = false) String currentOrderDirection,
                               @PageableDefault Pageable pageable
    ) {
        Pageable newPageable =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), activityUsersCounterService.setSort(orderField, currentOrderDirection));
        Page<ActivityUsersCounter> activityUsersCounters = activityUsersCounterService.countActivityUsers(newPageable);
        model.addAttribute("activities", activityUsersCounters);
        model.addAttribute("url", "/activity");
        model.addAttribute("currentOrderDirection", activityUsersCounterService.changeSortingDirection(currentOrderDirection));
        model.addAttribute("categories", categoryService.findAll());
        return "activity";
    }



    @GetMapping("/filter")
    public String filterActivityList(Model model,
                               @RequestParam(required = false) String filter,
                               @PageableDefault Pageable pageable
    ) {

//        Pageable newPageable =
//                PageRequest.of(3, 5, Sort.by("activityName").ascending());
//        Page<ActivityUsersCounter> activityUsersCounters = activityUsersCounterService.countActivityUsers(newPageable);
        model.addAttribute("activities", activityUsersCounterService.countActivityUsersByCategoryName(filter, pageable));
        model.addAttribute("currentOrderDirection", "ASC");
        model.addAttribute("url", "/activity");
        model.addAttribute("categories", categoryService.findAll());
        return "activity";
    }

    @PostMapping
    public String addActivity(Map<String, Object> model,
                              Activity activity,
                              @RequestParam Map<String, String> form) {
        try {
            for (String key : form.keySet()) {
                Category category = categoryService.loadCategoryByCategoryname(form.get(key));
                if (category != null) {
                    activity.setCategory(category);
                }
            }
            activityService.save(activity);
            model.put("addCategoryResult", "Activity successfully added");
        } catch (Exception ex) {
            model.put("addCategoryResult", "Activity not added");
        }
        return "redirect:/activity";
    }

    @PostMapping("{id}/time")
    public String userTime(
            @RequestParam("activityId") String activityId,
            @RequestParam("userId") String userId,
            @RequestParam("activity-start") String activityStart,
            @RequestParam("activity-end") String activityEnd
    ){
        UserActivityTime userActivityTime = new UserActivityTime(
                new AdminConfirmationKey(Long.valueOf(userId), Long.valueOf(activityId)),
                userService.findByUserId(Long.valueOf(userId)),
                activityService.findById(Long.valueOf(activityId)),
                LocalDateTime.parse(activityStart), LocalDateTime.parse(activityEnd));
        userActivityTimeService.save(userActivityTime);
        return ("redirect:/activity/" + userId);
    }

}
