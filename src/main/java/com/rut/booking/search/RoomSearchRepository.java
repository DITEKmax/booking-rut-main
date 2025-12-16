package com.rut.booking.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomSearchRepository extends ElasticsearchRepository<RoomDocument, String> {

    List<RoomDocument> findByNumberContainingOrBuildingContainingOrDescriptionContainingOrReviewsContaining(
            String number, String building, String description, String reviews);

    List<RoomDocument> findByIsActiveTrue();
}
