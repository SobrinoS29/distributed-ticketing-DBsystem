package edu.esi.ds.esientradas.dto;

public class ColaStatusDto {

    private String queueToken;
    private Long espectaculoId;
    private String status;
    private Integer position;
    private Integer aheadCount;
    private boolean canProceed;
    private Long turnExpiresAt;
    private String message;

    public ColaStatusDto() {
    }

    public ColaStatusDto(String queueToken, Long espectaculoId, String status, Integer position, Integer aheadCount, boolean canProceed, Long turnExpiresAt, String message) {
        this.queueToken = queueToken;
        this.espectaculoId = espectaculoId;
        this.status = status;
        this.position = position;
        this.aheadCount = aheadCount;
        this.canProceed = canProceed;
        this.turnExpiresAt = turnExpiresAt;
        this.message = message;
    }

    public String getQueueToken() {
        return queueToken;
    }

    public void setQueueToken(String queueToken) {
        this.queueToken = queueToken;
    }

    public Long getEspectaculoId() {
        return espectaculoId;
    }

    public void setEspectaculoId(Long espectaculoId) {
        this.espectaculoId = espectaculoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getAheadCount() {
        return aheadCount;
    }

    public void setAheadCount(Integer aheadCount) {
        this.aheadCount = aheadCount;
    }

    public boolean isCanProceed() {
        return canProceed;
    }

    public void setCanProceed(boolean canProceed) {
        this.canProceed = canProceed;
    }

    public Long getTurnExpiresAt() {
        return turnExpiresAt;
    }

    public void setTurnExpiresAt(Long turnExpiresAt) {
        this.turnExpiresAt = turnExpiresAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}