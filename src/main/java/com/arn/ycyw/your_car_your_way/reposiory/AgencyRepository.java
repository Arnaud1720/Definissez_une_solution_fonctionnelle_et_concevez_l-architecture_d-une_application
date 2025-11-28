package com.arn.ycyw.your_car_your_way.reposiory;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Integer> {
}
