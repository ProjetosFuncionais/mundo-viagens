package br.com.mundoviagens.web;
import br.com.mundoviagens.service.*;
import br.com.mundoviagens.web.Dtos.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.*;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
@RestController
@RequestMapping("/api/voos")
public class FlightController {
    private final FlightService flights;
    public FlightController(FlightService flights) { this.flights=flights; }
    @GetMapping
    public List<FlightService.Flight> search(@RequestParam(defaultValue="") String origem,
            @RequestParam(defaultValue="") String destino,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate data) {
        return flights.search(origem, destino, data);
    }
    @GetMapping("/{id}") public FlightService.Flight get(@PathVariable String id) { return flights.get(id); }
}
