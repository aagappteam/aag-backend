package aagapp_backend.services.social;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.social.UserVendorFollow;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.social.UserVendorFollowRepository;
import aagapp_backend.services.firebase.NotoficationFirebase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class FollowerNotificationService {

    @Autowired
    private UserVendorFollowRepository userVendorFollowRepository;

    @Autowired
    private NotoficationFirebase notificationFirebase;

    private final Executor executor = Executors.newFixedThreadPool(10); // Customize based on load


    @Async
    public void notifyFollowersInParallel(String contentType, String contentName, VendorEntity vendor) {
        int page = 0;
        int size = 100;
        Page<UserVendorFollow> followersPage;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        do {
            Pageable pageable = PageRequest.of(page, size);
            followersPage = userVendorFollowRepository.findFollowersByVendorId(vendor.getService_provider_id(), pageable);

            List<UserVendorFollow> chunk = followersPage.getContent();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (UserVendorFollow follow : chunk) {
                    CustomCustomer user = follow.getUser();
                    if (user != null && user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
                        try {
                            String safeName = Optional.ofNullable(contentName).orElse("an exciting event");
                            String vendorName = Optional.ofNullable(vendor.getFirst_name()).orElse("A vendor");

                            String title = "🎉 " + safeName + " is now Live!";
                            String body = vendorName + " just published a new " + contentType + ": " + safeName + ". Tap to join!";

                            notificationFirebase.sendNotification(user.getFcmToken(), title, body);



                        } catch (Exception e) {
                            System.err.println("❌ FCM error for userId " + user.getId() + ": " + e.getMessage());
                        }
                    }
                }
            }, executor);

            futures.add(future);
            page++;

        } while (followersPage.hasNext());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }


/*
    public void notifyFollowersInParallel(Game game, VendorEntity vendor) {
        int page = 0;
        int size = 100;
        Page<UserVendorFollow> followersPage;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        do {
            Pageable pageable = PageRequest.of(page, size);
            followersPage = userVendorFollowRepository.findFollowersByVendorId(vendor.getService_provider_id(), pageable);


            List<UserVendorFollow> chunk = followersPage.getContent();

            // Process this chunk asynchronously
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (UserVendorFollow follow : chunk) {
                    CustomCustomer user = follow.getUser();
                    if (user != null && user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
                        try {
                            String gameName = Optional.ofNullable(game.getName()).orElse("a new game");
                            String vendorName = Optional.ofNullable(vendor.getFirst_name()).orElse("A vendor");

                            String title = "🎮 " + gameName + " is now Live!";
                            String body = vendorName + " just launched " + gameName + ". Tap to check it out!";

                            notificationFirebase.sendNotification(user.getFcmToken(), title, body);

                        } catch (Exception e) {
                            System.err.println("Failed FCM to userId " + user.getId() + ": " + e.getMessage());
                        }
                    }
                }
            }, executor);

            futures.add(future);
            page++;

        } while (followersPage.hasNext());

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
*/

}

