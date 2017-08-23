package com.beimi.web.service.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beimi.web.model.GameAccountConfig;

public abstract interface GameAccountConfigRepository extends JpaRepository<GameAccountConfig, String>
{
  public abstract List<GameAccountConfig> findByOrgi(String orgi);
}
