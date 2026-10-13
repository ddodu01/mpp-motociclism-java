package ro.mpp2024.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import ro.mpp2024.Cursa;
import ro.mpp2024.repository.CursaRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/curse")
@CrossOrigin(origins = "http://localhost:5173")
public class CursaController {

    private final CursaRepository cursaRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public CursaController(CursaRepository cursaRepository, SimpMessagingTemplate messagingTemplate) {
        this.cursaRepository = cursaRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // GET /api/curse — afisarea tuturor
    @GetMapping
    public List<Cursa> getAll() {
        List<Cursa> curse = new ArrayList<>();
        cursaRepository.findAll().forEach(curse::add);
        return curse;
    }

    // GET /api/curse/{id} — cautare dupa id
    @GetMapping("/{id}")
    public ResponseEntity<Cursa> getById(@PathVariable Long id) {
        Cursa cursa = cursaRepository.findOne(id);
        if (cursa == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cursa);
    }

    // GET /api/curse/filter?capacitate=250 — filtrare dupa capacitate
    @GetMapping("/filter")
    public List<Cursa> filterByCapacitate(@RequestParam int capacitate) {
        List<Cursa> result = new ArrayList<>();
        cursaRepository.findAll().forEach(c -> {
            if (c.getCapacitateMotor() == capacitate) {
                result.add(c);
            }
        });
        return result;
    }

    // POST /api/curse — creare
    @PostMapping
    public ResponseEntity<Cursa> create(@RequestBody Cursa cursa) {
        cursa.setId(null);
        Cursa saved = cursaRepository.save(cursa);
        // Trimitere notificare WebSocket
        messagingTemplate.convertAndSend("/topic/curse", "ADDED");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/curse/{id} — modificare
    @PutMapping("/{id}")
    public ResponseEntity<Cursa> update(@PathVariable Long id, @RequestBody Cursa cursa) {
        Cursa existing = cursaRepository.findOne(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        cursa.setId(id);
        cursaRepository.update(cursa);
        // Trimitere notificare WebSocket
        messagingTemplate.convertAndSend("/topic/curse", "UPDATED");
        return ResponseEntity.ok(cursa);
    }

    // DELETE /api/curse/{id} — stergere
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Cursa existing = cursaRepository.findOne(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        cursaRepository.delete(id);
        // Trimitere notificare WebSocket
        messagingTemplate.convertAndSend("/topic/curse", "DELETED");
        return ResponseEntity.noContent().build();
    }
}