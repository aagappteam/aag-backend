package aagapp_backend.entity.game;

import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_rooms")
public class GameRoom {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @NotNull(message = "Room code cannot be null")
        @Column(name = "room_code", unique = true, nullable = false, length = 10)
        private String roomCode;

        @ManyToOne
        @JoinColumn(name = "player1_id", nullable = false)
        private Player player1;

        @ManyToOne
        @JoinColumn(name = "player2_id", nullable = true)
        private Player player2;

        @NotNull(message = "Status cannot be null")
        @Column(name = "status", nullable = false, length = 20)
        private String status;    // e.g., "waiting", "ongoing", "completed"

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getRoomCode() {
                return roomCode;
        }

        public void setRoomCode(String roomCode) {
                this.roomCode = roomCode;
        }

        public Player getPlayer1() {
                return player1;
        }

        public void setPlayer1(Player player1) {
                this.player1 = player1;
        }

        public Player getPlayer2() {
                return player2;
        }

        public void setPlayer2(Player player2) {
                this.player2 = player2;
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }

        public LocalDateTime getCreatedAt() {
                return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
        }

        public GameRoom(Long id, String roomCode, Player player1, Player player2, String status, LocalDateTime createdAt) {
                this.id = id;
                this.roomCode = roomCode;
                this.player1 = player1;
                this.player2 = player2;
                this.status = status;
                this.createdAt = createdAt;
        }

        public GameRoom(){

        }
}
