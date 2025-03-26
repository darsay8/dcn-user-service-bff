package dev.rm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.rm.model.Model;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

}
