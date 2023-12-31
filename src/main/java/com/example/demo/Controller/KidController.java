package com.example.demo.Controller;

import com.example.demo.Dto.AddKidRequest;
import com.example.demo.Dto.UpdateKidRequest;
import com.example.demo.Entity.Kid;
import com.example.demo.Service.KidService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小孩管理", description = "可控制小孩資料")
@RestController
public class KidController {
    @Autowired
    private KidService kidService;

    @GetMapping("/kid")
    public List<Kid> getAllKids(){
        return kidService.getAllKids();
    }

    @GetMapping("/kid/{Id}")
    public Kid getKid(@PathVariable int Id){
        return kidService.getKid(Id);
    }

    @PostMapping("/kid")
    public String createKid(@RequestBody AddKidRequest addKidRequest){
        String kidName = addKidRequest.getKidName();
        String fatherName = addKidRequest.getFatherName();
        return kidService.createKid(kidName, fatherName);
    }

    @PutMapping("/kid/{Id}")
    public String updateKid(@PathVariable int Id, @RequestBody UpdateKidRequest updateKidRequest){
        String kidName = updateKidRequest.getKidName();
        String fatherName = updateKidRequest.getFatherName();
        return kidService.updateKid(Id, kidName, fatherName);
    }

    @DeleteMapping("/kid/{Id}")
    public String deleteKid(@PathVariable int Id){
        return kidService.deleteKid(Id);
    }
}
