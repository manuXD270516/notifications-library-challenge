package com.novacomp.notifications.application.service;

import com.novacomp.notifications.domain.model.NotificationChannel;
import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Batch notification operations using advanced Stream API and functional programming.
 * 
 * <p>This class demonstrates Java 21 modern collection processing with:
 * <ul>
 *   <li><b>Stream API</b>: Functional transformations and aggregations</li>
 *   <li><b>Collectors</b>: Advanced data aggregation patterns</li>
 *   <li><b>Method References</b>: Concise function composition</li>
 *   <li><b>Predicates</b>: Functional filtering logic</li>
 *   <li><b>Records</b>: Immutable result objects</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * var batch = new NotificationBatch(notificationService);
 * 
 * List<NotificationRequest> requests = ...;
 * BatchResult result = batch.sendAll(requests);
 * 
 * System.out.println("Success rate: " + result.successRate());
 * result.failures().forEach(r -> System.out.println("Failed: " + r.errorMessage()));
 * }</pre>
 * 
 * @since 1.1.0
 */
@Slf4j
public class NotificationBatch {
    
    private final NotificationService notificationService;
    
    /**
     * Creates a new batch processor.
     * 
     * @param notificationService the notification service
     */
    public NotificationBatch(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Sends all requests and collects results into a batch result.
     * Demonstrates Stream API for batch processing.
     * 
     * @param requests collection of requests to send
     * @return batch result with statistics and individual results
     */
    public BatchResult sendAll(Collection<NotificationRequest> requests) {
        log.info("Processing batch of {} notifications", requests.size());
        
        List<NotificationResult> results = requests.stream()
            .map(notificationService::send)
            .toList();
        
        return new BatchResult(results);
    }
    
    /**
     * Sends requests filtered by a predicate.
     * Demonstrates functional filtering.
     * 
     * @param requests all requests
     * @param filter predicate to filter requests
     * @return batch result
     */
    public BatchResult sendFiltered(
        Collection<NotificationRequest> requests,
        Predicate<NotificationRequest> filter
    ) {
        log.info("Processing filtered batch");
        
        List<NotificationResult> results = requests.stream()
            .filter(filter)
            .map(notificationService::send)
            .toList();
        
        return new BatchResult(results);
    }
    
    /**
     * Sends requests grouped by channel.
     * Demonstrates Collectors.groupingBy for data organization.
     * 
     * @param requests requests to send
     * @return map of channel to batch results
     */
    public Map<NotificationChannel, BatchResult> sendGroupedByChannel(
        Collection<NotificationRequest> requests
    ) {
        log.info("Processing batch grouped by channel");
        
        return requests.stream()
            .collect(Collectors.groupingBy(
                NotificationRequest::channel,
                Collectors.collectingAndThen(
                    Collectors.mapping(
                        notificationService::send,
                        Collectors.toList()
                    ),
                    BatchResult::new
                )
            ));
    }
    
    /**
     * Sends requests and partitions results by success/failure.
     * Demonstrates Collectors.partitioningBy.
     * 
     * @param requests requests to send
     * @return partitioned results
     */
    public PartitionedResult sendPartitioned(Collection<NotificationRequest> requests) {
        log.info("Processing batch with partitioning");
        
        List<NotificationResult> results = requests.stream()
            .map(notificationService::send)
            .toList();
        
        Map<Boolean, List<NotificationResult>> partition = results.stream()
            .collect(Collectors.partitioningBy(NotificationResult::success));
        
        return new PartitionedResult(
            partition.get(true),
            partition.get(false)
        );
    }
    
    /**
     * Gets summary statistics for a batch.
     * Demonstrates custom Collector for complex aggregation.
     * 
     * @param requests requests to analyze
     * @return statistics summary
     */
    public BatchStatistics getStatistics(Collection<NotificationRequest> requests) {
        log.info("Calculating batch statistics");
        
        Map<NotificationChannel, Long> channelCounts = requests.stream()
            .collect(Collectors.groupingBy(
                NotificationRequest::channel,
                Collectors.counting()
            ));
        
        long totalRequests = requests.size();
        long uniqueRecipients = requests.stream()
            .map(NotificationRequest::recipient)
            .distinct()
            .count();
        
        return new BatchStatistics(totalRequests, uniqueRecipients, channelCounts);
    }
    
    /**
     * Immutable batch result record.
     * Contains all results and provides convenient access methods.
     * 
     * @param results all notification results
     */
    public record BatchResult(List<NotificationResult> results) {
        
        /**
         * Gets only successful results.
         * 
         * @return list of successful results
         */
        public List<NotificationResult> successes() {
            return results.stream()
                .filter(NotificationResult::success)
                .toList();
        }
        
        /**
         * Gets only failed results.
         * 
         * @return list of failed results
         */
        public List<NotificationResult> failures() {
            return results.stream()
                .filter(NotificationResult::isFailure)
                .toList();
        }
        
        /**
         * Gets count of successful sends.
         * 
         * @return success count
         */
        public long successCount() {
            return results.stream()
                .filter(NotificationResult::success)
                .count();
        }
        
        /**
         * Gets count of failed sends.
         * 
         * @return failure count
         */
        public long failureCount() {
            return results.stream()
                .filter(NotificationResult::isFailure)
                .count();
        }
        
        /**
         * Gets total count.
         * 
         * @return total count
         */
        public int totalCount() {
            return results.size();
        }
        
        /**
         * Calculates success rate as percentage.
         * 
         * @return success rate (0.0 to 100.0)
         */
        public double successRate() {
            if (results.isEmpty()) {
                return 0.0;
            }
            return (successCount() * 100.0) / results.size();
        }
        
        /**
         * Groups results by channel.
         * 
         * @return map of channel to results
         */
        public Map<NotificationChannel, List<NotificationResult>> byChannel() {
            return results.stream()
                .collect(Collectors.groupingBy(NotificationResult::channel));
        }
        
        /**
         * Gets all message IDs from successful sends.
         * 
         * @return list of message IDs
         */
        public List<String> messageIds() {
            return results.stream()
                .filter(NotificationResult::success)
                .map(NotificationResult::messageId)
                .flatMap(Optional::stream) // Flatten Optional to Stream
                .toList();
        }
    }
    
    /**
     * Partitioned results record.
     * Separates successful and failed results.
     * 
     * @param successes successful results
     * @param failures failed results
     */
    public record PartitionedResult(
        List<NotificationResult> successes,
        List<NotificationResult> failures
    ) {
        public long successCount() {
            return successes.size();
        }
        
        public long failureCount() {
            return failures.size();
        }
        
        public long totalCount() {
            return successCount() + failureCount();
        }
        
        public double successRate() {
            long total = totalCount();
            return total == 0 ? 0.0 : (successCount() * 100.0) / total;
        }
    }
    
    /**
     * Batch statistics record.
     * Provides insights into batch composition.
     * 
     * @param totalRequests total number of requests
     * @param uniqueRecipients count of unique recipients
     * @param channelDistribution distribution of requests by channel
     */
    public record BatchStatistics(
        long totalRequests,
        long uniqueRecipients,
        Map<NotificationChannel, Long> channelDistribution
    ) {
        /**
         * Gets the most used channel.
         * 
         * @return Optional containing the most used channel
         */
        public Optional<NotificationChannel> mostUsedChannel() {
            return channelDistribution.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        }
        
        /**
         * Gets channels sorted by usage.
         * 
         * @return list of channels sorted by usage (descending)
         */
        public List<NotificationChannel> channelsByUsage() {
            return channelDistribution.entrySet()
                .stream()
                .sorted(Map.Entry.<NotificationChannel, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
        }
    }
}
