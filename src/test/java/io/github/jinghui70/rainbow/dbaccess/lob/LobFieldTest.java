package io.github.jinghui70.rainbow.dbaccess.lob;

import cn.hutool.core.collection.ListUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BlobObjectFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.ClobObjectFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LobFieldTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "BLOB_OBJECT", "CLOB_OBJECT");
    }

    private final String contentStr = "在不远的未来，人类终于解锁了时间旅行的秘密。这一发现立即引起了全球的轰动，科学家们、历史学家们、甚至普通大众都对能够亲眼目睹历史或探索未来的可能性感到兴奋不已。然而，随着技术的发展，政府和国际组织很快意识到，时间旅行如果不受控制，可能会对现实造成不可逆转的影响。" +
            "因此，成立了一个名为“时空监护组织”的全球性机构，负责监管时间旅行的所有活动。他们制定了严格的规则，比如禁止任何形式的“时间悖论”行为，即任何可能改变历史进程的行为。此外，时空监护组织还设立了“历史观察者”项目，允许经过严格训练的旅行者回到过去，但只能作为观察者，不能与历史人物互动或改变任何事件。" +
            "在这个时代，有一个年轻的物理学家，名叫艾丽娅。她对时间旅行充满了热情，梦想着能够见证历史的重大时刻。经过多年的努力，她终于通过了时空监护组织的所有测试，成为了一名正式的历史观察者。她的第一次任务是回到20世纪的某个关键时刻，观察一个改变了世界的历史事件。" +
            "艾丽娅被送到了1945年的柏林，她的目标是亲眼目睹二战的结束。她穿着当时的服装，混入了人群之中，小心翼翼地不让自己的存在引起任何注意。随着苏联红军的进入，城市中充满了混乱和恐慌，但艾丽娅保持着冷静，她知道自己的任务是观察，而不是干预。" +
            "就在这时，她目睹了一个改变她一生的事件。一个年轻的士兵，在战火中救出了一个小女孩。这个士兵的英勇行为，以及他对生命的尊重，深深地触动了艾丽娅。她意识到，尽管时间旅行有着严格的规则，但人类的勇气和善良是跨越时间的永恒价值。" +
            "艾丽娅回到了自己的时代，她的经历被严格记录和封存，以防止任何可能的历史泄露。但她心中始终保留着那次旅行的记忆，它提醒着她，无论科技如何进步，人类的情感和道德选择才是推动历史前进的真正力量。" +
            "这个故事虽然是虚构的，但它探讨了时间旅行可能带来的伦理和道德问题，以及人类对于历史的尊重和保护的重要性。";

    private final List<SimpleObject> objectList = ListUtil.of(
                new SimpleObject(10, "Tom", new Double[]{100.0, 99.0, 89.0}),
                new SimpleObject(20, "Jerry", new Double[]{60.0, 61.0, 62.0})
        );

    private void assertTom(SimpleObject so) {
        assertEquals(10, so.getId());
        assertEquals("Tom", so.getName());
        assertEquals(100, so.getScore()[0]);
        assertEquals(99, so.getScore()[1]);
        assertEquals(89, so.getScore()[2]);
    }

    private void assertJerry(SimpleObject so) {
        assertEquals(20, so.getId());
        assertEquals("Jerry", so.getName());
        assertEquals(60, so.getScore()[0]);
        assertEquals(61, so.getScore()[1]);
        assertEquals(62, so.getScore()[2]);
    }

    @Test
    public void testBlob() {
        byte[] contentByte = contentStr.getBytes(StandardCharsets.UTF_8);
        BlobObject obj = new BlobObject();

        obj.setId(1);
        obj.setLobString(contentStr);
        obj.setLobByteArray(contentByte);
        obj.setLobObject(objectList.get(0));
        obj.setLobArray(objectList);
        dba.insert(obj);

        obj = dba.select(BlobObject.class).where("id", 1).queryForObject();
        assertEquals(contentStr, obj.getLobString());
        assertArrayEquals(contentByte, obj.getLobByteArray());
        assertTom(obj.getLobObject());
        List<SimpleObject> list = obj.getLobArray();
        assertEquals(2, list.size());
        assertTom(list.get(0));
        assertJerry(list.get(1));

        // 测试单个字段提取
        SimpleObject so = dba.select("LOB_OBJECT").from(BlobObject.class).where("id", 1)
                .queryForValue(BlobObjectFieldMapper.of(SimpleObject.class));
        assertTom(so);

        SimpleObject[] array = dba.select("LOB_ARRAY").from(BlobObject.class).where("id", 1)
                .queryForValue(BlobObjectFieldMapper.ofArray(SimpleObject.class));
        assertEquals(2, array.length);
        assertTom(array[0]);
        assertJerry(array[1]);
    }

    @Test
    public void testClob() {
        ClobObject obj = new ClobObject();

        obj.setId(1);
        obj.setLobString(contentStr);
        obj.setLobObject(objectList.get(0));
        obj.setLobArray(objectList.toArray(new SimpleObject[0]));
        dba.insert(obj);

        obj = dba.select(ClobObject.class).where("id", 1).queryForObject();
        assertEquals(contentStr, obj.getLobString());
        assertTom(obj.getLobObject());
        SimpleObject[] array = obj.getLobArray();
        assertEquals(2, array.length);
        assertTom(array[0]);
        assertJerry(array[1]);

        // 测试单个字段提取
        SimpleObject so = dba.select("LOB_OBJECT").from(ClobObject.class).where("id", 1)
                .queryForValue(ClobObjectFieldMapper.of(SimpleObject.class));
        assertTom(so);

        List<SimpleObject> list = dba.select("LOB_ARRAY").from(ClobObject.class).where("id", 1)
                .queryForValue(ClobObjectFieldMapper.ofList(SimpleObject.class));
        assertEquals(2, list.size());
        assertTom(list.get(0));
        assertJerry(list.get(1));
    }

}
