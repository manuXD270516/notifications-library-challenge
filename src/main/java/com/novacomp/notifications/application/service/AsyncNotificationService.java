package com.novacomp.notifications.application.service;

import com.novacomp.notifications.domain.model.NotificationRequest;
import com.novacomp.notifications.domain.model.NotificationResult;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Async notification service using Java 21 Virtual Threads for high-performance concurrent operations.
 * 
 * <p>Virtual threads (Project Loom - Java 21) enable massive concurrency with minimal overhead.
 * Unlike platform threads, millions of virtual threads can be created without memory concerns.
 * 
 * <p>Key Java 21 features demonstrated:
 * <ul>
 *   <li><b>Virtual Threads</b>: {@code Executors.newVirtualThreadPerTaskExecutor()}</li>
 *   <li><b>CompletableFuture</b>: Advanced async composition</li>
 *   <li><b>Stream API</b>: Functional collection processing</li>
 *   <li><b>Method References</b>: Concise lambda expressions</li>
 *   <li><b>Try-with-resources</b>: Automatic executor cleanup</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * var asyncService = new AsyncNotificationService(notificationService);
 * 
 * // Send async
 * CompletableFuture<NotificationResult> future = asyncService.sendAsync(request);
 * future.thenAccept(result -> System.out.println("Sent: " + result.messageId()));
 * 
 * // Send batch with virtual threads
 * List<NotificationRequest> requests = List.of(req1, req2, req3);
 * CompletableFuture<List<NotificationResult>> batchFuture = 
 *     asyncService.sendBatch(requests);
 * }</pre>
 * 
 * @see NotificationService
 * @since 1.1.0
 */
@Slf4j
public class AsyncNotificationService implements AutoCloseable {
    
    private final NotificationService notificationService;
    private final ExecutorService virtualThreadExecutor;
    
    /**
     * Creates a new async notification service with virtual threads.
     * 
     * <p>Virtual threads are created per task, providing excellent scalability.
     * Each notification is sent in its own virtual thread, allowing thousands of
     * concurrent operations without thread pool exhaustion.
     * 
     * @param notificationService the underlying sync notification service
     */
    public AsyncNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        // Java 21: Virtual threads - lightweight, scalable concurrency
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        log.info("AsyncNotificationService initialized with virtual threads");
    }
    
    /**
     * Sends a notification asynchronously using a virtual thread.
     * 
     * <p>Returns immediately with a CompletableFuture that completes when the
     * notification is sent. This is non-blocking and highly scalable.
     * 
     * @param request the notification request
     * @return CompletableFuture that completes with the result
     */
    public CompletableFuture<NotificationResult> sendAsync(NotificationRequest request) {
        log.debug("Scheduling async notification for channel: {}", request.channel());
        
        return CompletableFuture.supplyAsync(
            () -> {
                log.trace("Executing notification in virtual thread: {}", Thread.currentThread());
                return notificationService.send(request);
            },
            virtualThreadExecutor
        ).whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Async notification failed", throwable);
            } else {
                log.debug("Async notification completed: {}", result.toLogString());
            }
        });
    }
    
    /**
     * Sends multiple notifications concurrently using virtual threads.
     * 
     * <p>Each notification is sent in its own virtual thread, achieving true parallelism.
     * This method demonstrates the power of virtual threads - you can send thousands
     * of notifications concurrently without worrying about thread pool limits.
     * 
     * <p>Java 21 features demonstrated:
     * <ul>
     *   <li>Stream API for functional processing</li>
     *   <li>Virtual threads for massive concurrency</li>
     *   <li>CompletableFuture.allOf for coordinated completion</li>
     *   <li>Method references for concise lambdas</li>
     * </ul>
     * 
     * @param requests collection of notification requests
     * @return CompletableFuture that completes when all notifications are sent
     */
    public CompletableFuture<List<NotificationResult>> sendBatch(
        Collection<NotificationRequest> requests
    ) {
        log.info("Sending batch of {} notifications using virtual threads", requests.size());
        
        // Create a CompletableFuture for each request
        List<CompletableFuture<NotificationResult>> futures = requests.stream()
            .map(this::sendAsync)
            .toList(); // Java 16+: Stream.toList() for immutable list
        
        // Wait for all to complete, then collect results
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join) // Get completed results
                .toList()
            );
    }
    
    /**
     * Sends notifications with a timeout.
     * 
     * <p>Demonstrates advanced CompletableFuture usage with timeout handling.
     * If the operation doesn't complete within the specified duration, the future
     * completes exceptionally.
     * 
     * @param request the notification request
     * @param timeout maximum time to wait
     * @return CompletableFuture that completes with result or times out
     */
    public CompletableFuture<NotificationResult> sendWithTimeout(
        NotificationRequest request,
        Duration timeout
    ) {
        return sendAsync(request)
            .orTimeout(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .exceptionally(throwable -> {
                log.warn("Notification timed out after {}: {}", timeout, request.channel());
                return NotificationResult.failure(
                    request.channel(),
                    "Timeout after " + timeout
                );
            });
    }
    
    /**
     * Sends notifications and returns only successful results.
     * 
     * <p>Demonstrates Stream API filtering and functional composition.
     * Failed notifications are logged but not included in the result.
     * 
     * @param requests collection of notification requests
     * @return CompletableFuture with only successful results
     */
    public CompletableFuture<List<NotificationResult>> sendBatchSuccessfulOnly(
        Collection<NotificationRequest> requests
    ) {
        return sendBatch(requests)
            .thenApply(results -> results.stream()
                .filter(NotificationResult::success)
                .peek(result -> log.debug("Successful: {}", result.messageId()))
                .toList()
            );
    }
    
    /**
     * Sends notifications and groups results by success/failure.
     * 
     * <p>Demonstrates advanced Stream API with Collectors.partitioningBy.
     * Returns a map with two keys: true (successful) and false (failed).
     * 
     * @param requests collection of notification requests
     * @return CompletableFuture with results partitioned by success
     */
    public CompletableFuture<java.util.Map<Boolean, List<NotificationResult>>> 
        sendBatchPartitioned(Collection<NotificationRequest> requests) {
        
        return sendBatch(requests)
            .thenApply(results -> results.stream()
                .collect(Collectors.partitioningBy(NotificationResult::success))
            )
            .whenComplete((partition, throwable) -> {
                if (partition != null) {
                    log.info("Batch complete: {} successful, {} failed",
                        partition.get(true).size(),
                        partition.get(false).size()
                    );
                }
            });
    }
    
    /**
     * Sends notifications in parallel with progress tracking.
     * 
     * <p>Demonstrates functional composition with side effects (logging).
     * Each completion is logged to provide real-time progress visibility.
     * 
     * @param requests collection of notification requests
     * @return CompletableFuture with all results
     */
    public CompletableFuture<List<NotificationResult>> sendBatchWithProgress(
        Collection<NotificationRequest> requests
    ) {
        log.info("Starting batch of {} notifications", requests.size());
        
        var counter = new java.util.concurrent.atomic.AtomicInteger(0);
        var total = requests.size();
        
        List<CompletableFuture<NotificationResult>> futures = requests.stream()
            .map(req -> sendAsync(req)
                .whenComplete((result, throwable) -> {
                    int completed = counter.incrementAndGet();
                    log.info("Progress: {}/{} completed", completed, total);
                })
            )
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList()
            );
    }
    
    /**
     * Closes the executor service gracefully.
     * 
     * <p>Implements AutoCloseable for try-with-resources pattern.
     * Virtual thread executor shuts down immediately as virtual threads are cheap.
     */
    @Override
    public void close() {
        log.info("Shutting down virtual thread executor");
        virtualThreadExecutor.close(); // Java 21: ExecutorService implements AutoCloseable
    }
    
    /**
     * Gets the executor service for advanced usage.
     * 
     * @return the virtual thread executor
     */
    public ExecutorService getExecutor() {
        return virtualThreadExecutor;
    }
}
