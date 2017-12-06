package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.GamePlaywayGroup;
import com.beimi.web.model.GamePlaywayGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GamePlaywayGroupItemRepository extends JpaRepository<GamePlaywayGroupItem, String>{

  public abstract GamePlaywayGroupItem findByIdAndOrgi(String id, String orgi);

  public void deleteByPlaywayidAndOrgi(String playwayid, String orgi) ;

  public abstract int countByNameAndPlaywayidAndOrgi(String name, String playwayid, String orgi);

  public abstract int countByNameAndPlaywayidAndOrgiNotAndId(String name, String playwayid, String orgi , String id);

  public abstract List<GamePlaywayGroupItem> findByOrgiAndPlaywayid(String orgi, String playwayid);
}
