package com.cxb.voice.model;

import com.cxb.voice.enums.Status;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "voice")
public class Voice implements Serializable {
    private static final long serialVersionUID = -95652700480038235L;
    /**
     * id
     */
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Integer id;

    @Column(name = "file_name")
    @Getter @Setter
    private String fileName;

    @Column(name = "url")
    @Getter @Setter
    private String url;

    @Column(name = "task_id", unique = true)
    @Getter @Setter
    private String taskId;

    @Column(name = "result")
    @Type(type = "text")
    @Getter @Setter
    private String result;

    @Column(name = "status")
    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    @Getter @Setter
    private Date createTime;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    @Getter @Setter
    private Date updateTime;

    @Version
    @Getter @Setter
    private Integer version;
}
