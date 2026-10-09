package cc.wutao.mapper;

import cc.wutao.entity.AiCoverAsset;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiCoverAssetMapper {

    @Insert("""
            insert into ai_cover_assets(id, owner_id, object_key, status, expires_at)
            values(#{id}, #{ownerId}, #{objectKey}, #{status}, #{expiresAt})
            """)
    void insert(AiCoverAsset asset);

    @Update("""
            update ai_cover_assets set image_url = #{url}, status = 'CANDIDATE'
            where id = #{id} and status = 'UPLOADING'
            """)
    int completeUpload(@Param("id") String id, @Param("url") String url);

    @Select("select * from ai_cover_assets where id = #{id} for update")
    AiCoverAsset getByIdForUpdate(String id);

    @Select("select * from ai_cover_assets where image_url = #{url} for update")
    AiCoverAsset getByImageUrlForUpdate(String url);

    @Update("""
            update ai_cover_assets set article_id = #{articleId}, status = 'ATTACHED'
            where id = #{id}
            """)
    void attach(@Param("id") String id, @Param("articleId") Long articleId);

    @Update("""
            <script>
            update ai_cover_assets set status = 'DELETING'
            where article_id = #{articleId} and status = 'ATTACHED'
            <if test="keepId != null">and id != #{keepId}</if>
            </script>
            """)
    void markReplaced(@Param("articleId") Long articleId, @Param("keepId") String keepId);

    @Update("update ai_cover_assets set status = 'DELETING' where id = #{id}")
    void markDeleting(String id);

    @Update("""
            update ai_cover_assets set status = 'DELETING'
            where status in ('UPLOADING', 'CANDIDATE') and expires_at <= now()
            """)
    void markExpired();

    @Select("""
            select * from ai_cover_assets where status = 'DELETING'
            and retry_at <= now() order by retry_at, id limit 50
            """)
    List<AiCoverAsset> findDeleting();

    @Delete("delete from ai_cover_assets where id = #{id} and status = 'DELETING'")
    void deleteDeleting(String id);

    @Update("""
            update ai_cover_assets set retry_at = date_add(now(), interval 5 minute)
            where id = #{id} and status = 'DELETING'
            """)
    void deferRetry(String id);
}
