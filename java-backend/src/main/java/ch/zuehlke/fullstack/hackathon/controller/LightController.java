package ch.zuehlke.fullstack.hackathon.controller;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.light.model.LightSwitch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/led")
@RequiredArgsConstructor
@Slf4j
public class LightController {

    private LightSwitch lightSwitch;

    @Autowired
    public LightController(LightSwitch lightSwitch) {
        this.lightSwitch = lightSwitch;
    }

    @Operation(summary = "Example demo DTO",
            description = "This can be used to enrich swagger documentation")
    @ApiResponse(responseCode = "200", description = "Successfully returned example")
    @ApiResponse(responseCode = "500", description = "Something failed internally")
    @GetMapping("")
    public ResponseEntity<String> getLightStatus() {
        var color = lightSwitch.getColor().toString();
        return new ResponseEntity<>(color, HttpStatus.OK);
    }
}