package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.DataDic;
import com.beimi.web.model.GamePlaywayGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GamePlaywayGroupRepository extends JpaRepository<GamePlaywayGroup, String>{

  public abstract GamePlaywayGroup findByIdAndOrgi(String id, String orgi);

  public abstract int countByNameAndPlaywayidAndOrgi(String name , String playwayid, String orgi);

  public abstract int countByNameAndPlaywayidAndOrgiNotAndId(String name , String playwayid, String orgi , String id);

  public abstract List<GamePlaywayGroup> findByOrgiAndPlaywayid(String orgi, String playwayid);
}
