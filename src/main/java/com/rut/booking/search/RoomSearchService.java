package com.rut.booking.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.rut.booking.models.entities.Review;
import com.rut.booking.models.entities.Room;
import com.rut.booking.repository.ReviewRepository;
import com.rut.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomSearchService {

    private final RoomSearchRepository roomSearchRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Index all rooms from the database to Elasticsearch
     */
    @PostConstruct
    public void indexAllRooms() {
        try {
            List<Room> rooms = roomRepository.findAll();
            for (Room room : rooms) {
                indexRoom(room);
            }
            log.info("Indexed {} rooms to Elasticsearch", rooms.size());
        } catch (Exception e) {
            log.warn("Could not index rooms to Elasticsearch. Make sure Elasticsearch is running: {}", e.getMessage());
        }
    }

    /**
     * Index a single room
     */
    public void indexRoom(Room room) {
        try {
            // Get all reviews for this room
            List<Review> reviews = reviewRepository.findByRoomId(room.getId());
            String reviewTexts = reviews.stream()
                    .map(Review::getComment)
                    .filter(comment -> comment != null && !comment.isEmpty())
                    .collect(Collectors.joining(" "));

            Double avgRating = reviewRepository.getAverageRatingForRoom(room.getId());
            Integer reviewCount = reviews.size();

            // Build equipment text for searchability
            StringBuilder equipmentText = new StringBuilder();
            if (Boolean.TRUE.equals(room.getHasProjector())) {
                equipmentText.append("проектор projector ");
            }
            if (Boolean.TRUE.equals(room.getHasComputers())) {
                equipmentText.append("компьютеры computers ");
            }
            if (Boolean.TRUE.equals(room.getHasWhiteboard())) {
                equipmentText.append("доска whiteboard маркеры markers ");
            }

            RoomDocument document = RoomDocument.builder()
                    .id(room.getId().toString())
                    .number(room.getNumber())
                    .building(room.getBuilding())
                    .floor(room.getFloor())
                    .capacity(room.getCapacity())
                    .roomType(room.getRoomType().name())
                    .roomTypeDisplayName(room.getRoomType().getDisplayName())
                    .description(room.getDescription())
                    .hasProjector(room.getHasProjector())
                    .hasComputers(room.getHasComputers())
                    .hasWhiteboard(room.getHasWhiteboard())
                    .averageRating(avgRating != null ? avgRating : 0.0)
                    .reviewCount(reviewCount)
                    .reviews(reviewTexts)
                    .equipmentText(equipmentText.toString())
                    .isActive(room.getIsActive())
                    .build();

            roomSearchRepository.save(document);
        } catch (Exception e) {
            log.warn("Could not index room {} to Elasticsearch: {}", room.getId(), e.getMessage());
        }
    }

    /**
     * Search rooms by keyword (searches in number, building, description, and reviews)
     */
    public List<Long> searchRooms(String keyword) {
        try {
            Query multiMatchQuery = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("number^3", "building^2", "equipmentText^2", "description", "reviews", "roomTypeDisplayName")
                    .fuzziness("AUTO")
            )._toQuery();

            Query boolQuery = BoolQuery.of(b -> b
                    .must(multiMatchQuery)
                    .filter(f -> f.term(t -> t.field("isActive").value(true)))
            )._toQuery();

            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(boolQuery)
                    .build();

            SearchHits<RoomDocument> searchHits = elasticsearchOperations.search(
                    searchQuery, RoomDocument.class);

            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(doc -> Long.parseLong(doc.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Elasticsearch search failed, falling back to database search: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Delete a room from the index
     */
    public void deleteRoom(Long roomId) {
        try {
            roomSearchRepository.deleteById(roomId.toString());
        } catch (Exception e) {
            log.warn("Could not delete room {} from Elasticsearch: {}", roomId, e.getMessage());
        }
    }

    /**
     * Re-index a room when a review is added or updated
     */
    public void reindexRoomAfterReview(Long roomId) {
        try {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room != null) {
                indexRoom(room);
            }
        } catch (Exception e) {
            log.warn("Could not reindex room {} after review: {}", roomId, e.getMessage());
        }
    }
}
