package com.Proyecto.stoq.infrastructure.adapters;

import org.springframework.stereotype.Repository;
import com.Proyecto.stoq.domain.ports.UsuarioRepositoryPort;
import com.Proyecto.stoq.infrastructure.persistence.repositories.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.Proyecto.stoq.domain.model.Usuario;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioRepository repository;

    public UsuarioRepositoryAdapter(UsuarioRepository repository){
        this.repository = repository;
    }

    @Override
    public List<Usuario> findAll(){
        return repository.findAll();
    }

    @Override
    public Optional<Usuario> findById(UUID id){
        return repository.findById(id);
    }

    @Override
    public Usuario save(Usuario usuario){
        return repository.save(usuario);
    }

    @Override
    public void deleteById(UUID id){
        repository.deleteById(id);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return repository.findByCorreo(correo);
    }
}