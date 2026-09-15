-- 按分类更新图片URL，使用Unsplash公开图片，使图片内容与分类匹配
-- 分类：1=风景 2=人物 3=动物 4=建筑 5=美食 6=科技 7=艺术 8=其他

-- 风景类 (category_id=1, image id 1-7)
UPDATE image SET url='https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop', tags='风景,自然,山脉' WHERE id=1;
UPDATE image SET url='https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=400&h=300&fit=crop', tags='风景,湖泊,森林' WHERE id=2;
UPDATE image SET url='https://images.unsplash.com/photo-1469474968028-56623f875231?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1469474968028-56623f875231?w=400&h=300&fit=crop', tags='风景,日落,自然' WHERE id=3;
UPDATE image SET url='https://images.unsplash.com/photo-1501785880801-7f6774d85f4b?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1501785880801-7f6774d85f4b?w=400&h=300&fit=crop', tags='风景,海洋,海岸' WHERE id=4;
UPDATE image SET url='https://images.unsplash.com/photo-1518495973542-b5f2448e48ed?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518495973542-b5f2448e48ed?w=400&h=300&fit=crop', tags='风景,山谷,绿色' WHERE id=5;
UPDATE image SET url='https://images.unsplash.com/photo-1433086966476-14ca9a34d9ed?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1433086966476-14ca9a34d9ed?w=400&h=300&fit=crop', tags='风景,瀑布,溪流' WHERE id=6;
UPDATE image SET url='https://images.unsplash.com/photo-1475926388450-9d7a7b3b8f1e?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1475926388450-9d7a7b3b8f1e?w=400&h=300&fit=crop', tags='风景,田野,乡村' WHERE id=7;

-- 人物类 (category_id=2, image id 8-13)
UPDATE image SET url='https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=300&fit=crop', tags='人物,女性,肖像' WHERE id=8;
UPDATE image SET url='https://images.unsplash.com/photo-1507003211169-0a1dd7d7b3f3?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1507003211169-0a1dd7d7b3f3?w=400&h=300&fit=crop', tags='人物,男性,肖像' WHERE id=9;
UPDATE image SET url='https://images.unsplash.com/photo-1438761681033-6471a05d7e92?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1438761681033-6471a05d7e92?w=400&h=300&fit=crop', tags='人物,儿童,快乐' WHERE id=10;
UPDATE image SET url='https://images.unsplash.com/photo-1534570202696-0f1d9e33d417?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1534570202696-0f1d9e33d417?w=400&h=300&fit=crop', tags='人物,女性,时尚' WHERE id=11;
UPDATE image SET url='https://images.unsplash.com/photo-1500648767791-00dcc994d835?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1500648767791-00dcc994d835?w=400&h=300&fit=crop', tags='人物,男性,商务' WHERE id=12;
UPDATE image SET url='https://images.unsplash.com/photo-1517070208541-5a6e7b3e4b80?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1517070208541-5a6e7b3e4b80?w=400&h=300&fit=crop', tags='人物,情侣,生活' WHERE id=13;

-- 动物类 (category_id=3, image id 14-19)
UPDATE image SET url='https://images.unsplash.com/photo-1547475317-2b9e4b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1547475317-2b9e4b3b3e3a?w=400&h=300&fit=crop', tags='动物,猫,宠物' WHERE id=14;
UPDATE image SET url='https://images.unsplash.com/photo-1552053831-71594a922f2b?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1552053831-71594a922f2b?w=400&h=300&fit=crop', tags='动物,狗,宠物' WHERE id=15;
UPDATE image SET url='https://images.unsplash.com/photo-1534151123215-5b58e6ea4f6a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1534151123215-5b58e6ea4f6a?w=400&h=300&fit=crop', tags='动物,鸟,自然' WHERE id=16;
UPDATE image SET url='https://images.unsplash.com/photo-1574158622681-a1b8a8b3e8a3?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1574158622681-a1b8a8b3e8a3?w=400&h=300&fit=crop', tags='动物,兔子,可爱' WHERE id=17;
UPDATE image SET url='https://images.unsplash.com/photo-1425082662705-729b7f8c8f2c?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1425082662705-729b7f8c8f2c?w=400&h=300&fit=crop', tags='动物,马,草原' WHERE id=18;
UPDATE image SET url='https://images.unsplash.com/photo-1474511327277-8437f3b65f1b?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1474511327277-8437f3b65f1b?w=400&h=300&fit=crop', tags='动物,鹿,森林' WHERE id=19;

-- 建筑类 (category_id=4, image id 20-25)
UPDATE image SET url='https://images.unsplash.com/photo-1486406146-2a02f5b2e2e1?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1486406146-2a02f5b2e2e1?w=400&h=300&fit=crop', tags='建筑,现代,城市' WHERE id=20;
UPDATE image SET url='https://images.unsplash.com/photo-1545324418-cc1c3212f5bd?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1545324418-cc1c3212f5bd?w=400&h=300&fit=crop', tags='建筑,高楼,商务' WHERE id=21;
UPDATE image SET url='https://images.unsplash.com/photo-1487958449943-2b33f6c6e3a1?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1487958449943-2b33f6c6e3a1?w=400&h=300&fit=crop', tags='建筑,古典,欧式' WHERE id=22;
UPDATE image SET url='https://images.unsplash.com/photo-1439070125227-72f6db2e0b4a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1439070125227-72f6db2e0b4a?w=400&h=300&fit=crop', tags='建筑,桥梁,工程' WHERE id=23;
UPDATE image SET url='https://images.unsplash.com/photo-1515263487995-2228f45e8fd3?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1515263487995-2228f45e8fd3?w=400&h=300&fit=crop', tags='建筑,室内,设计' WHERE id=24;
UPDATE image SET url='https://images.unsplash.com/photo-1496564203e5b2be5e5f3e5a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1496564203e5b2be5e5f3e5a?w=400&h=300&fit=crop', tags='建筑,教堂,历史' WHERE id=25;

-- 美食类 (category_id=5, image id 26-31)
UPDATE image SET url='https://images.unsplash.com/photo-1565299624946-8e2f2b3b3b1a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1565299624946-8e2f2b3b3b1a?w=400&h=300&fit=crop', tags='美食,披萨,意式' WHERE id=26;
UPDATE image SET url='https://images.unsplash.com/photo-1546069900159-2c8f9e3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1546069900159-2c8f9e3b3e3a?w=400&h=300&fit=crop', tags='美食,沙拉,健康' WHERE id=27;
UPDATE image SET url='https://images.unsplash.com/photo-1504674903325-9c3f5e3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1504674903325-9c3f5e3b3e3a?w=400&h=300&fit=crop', tags='美食,汉堡,快餐' WHERE id=28;
UPDATE image SET url='https://images.unsplash.com/photo-1551782450-a2133b0c1e8a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1551782450-a2133b0c1e8a?w=400&h=300&fit=crop', tags='美食,烧烤,烤肉' WHERE id=29;
UPDATE image SET url='https://images.unsplash.com/photo-1565958011703-9ae1b3e3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1565958011703-9ae1b3e3b3e3a?w=400&h=300&fit=crop', tags='美食,甜点,蛋糕' WHERE id=30;
UPDATE image SET url='https://images.unsplash.com/photo-1517248135467-4c6b3a3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1517248135467-4c6b3a3b3e3a?w=400&h=300&fit=crop', tags='美食,咖啡,饮品' WHERE id=31;

-- 科技类 (category_id=6, image id 32-37)
UPDATE image SET url='https://images.unsplash.com/photo-1518770660444-4a5f4f3b3b1a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518770660444-4a5f4f3b3b1a?w=400&h=300&fit=crop', tags='科技,电脑,编程' WHERE id=32;
UPDATE image SET url='https://images.unsplash.com/photo-1526374965602-3f8f3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1526374965602-3f8f3b3b3e3a?w=400&h=300&fit=crop', tags='科技,手机,智能' WHERE id=33;
UPDATE image SET url='https://images.unsplash.com/photo-1518779594330-4b3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518779594330-4b3b3b3b3e3a?w=400&h=300&fit=crop', tags='科技,电路,电子' WHERE id=34;
UPDATE image SET url='https://images.unsplash.com/photo-1535223289810-3b3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1535223289810-3b3b3b3b3e3a?w=400&h=300&fit=crop', tags='科技,服务器,数据中心' WHERE id=35;
UPDATE image SET url='https://images.unsplash.com/photo-1518779594330-3b3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518779594330-3b3b3b3b3e3a?w=400&h=300&fit=crop', tags='科技,机器人,智能' WHERE id=36;
UPDATE image SET url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=400&h=300&fit=crop', tags='科技,VR,虚拟现实' WHERE id=37;

-- 艺术类 (category_id=7, image id 38-43)
UPDATE image SET url='https://images.unsplash.com/photo-1513364724556-3b3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1513364724556-3b3b3b3b3e3a?w=400&h=300&fit=crop', tags='艺术,绘画,色彩' WHERE id=38;
UPDATE image SET url='https://images.unsplash.com/photo-1547824890-2c3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1547824890-2c3b3b3b3e3a?w=400&h=300&fit=crop', tags='艺术,雕塑,现代' WHERE id=39;
UPDATE image SET url='https://images.unsplash.com/photo-1518998493731-3b3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518998493731-3b3b3b3b3e3a?w=400&h=300&fit=crop', tags='艺术,抽象,创意' WHERE id=40;
UPDATE image SET url='https://images.unsplash.com/photo-1513364724556-3b3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1513364724556-3b3b3b3b3e3a?w=400&h=300&fit=crop', tags='艺术,油画,风景' WHERE id=41;
UPDATE image SET url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=400&h=300&fit=crop', tags='艺术,设计,排版' WHERE id=42;
UPDATE image SET url='https://images.unsplash.com/photo-1513364724556-3b3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1513364724556-3b3b3b3b3e3a?w=400&h=300&fit=crop', tags='艺术,摄影,黑白' WHERE id=43;

-- 其他类 (category_id=8, image id 44-50)
UPDATE image SET url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=400&h=300&fit=crop', tags='其他,综合,生活' WHERE id=44;
UPDATE image SET url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=400&h=300&fit=crop', tags='其他,综合,旅行' WHERE id=45;
UPDATE image SET url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=400&h=300&fit=crop', tags='其他,综合,抽象' WHERE id=46;
UPDATE image SET url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=400&h=300&fit=crop', tags='其他,综合,纹理' WHERE id=47;
UPDATE image SET url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=400&h=300&fit=crop', tags='其他,综合,极简' WHERE id=48;
UPDATE image SET url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1526378722-3e3b3b3b3e3a?w=400&h=300&fit=crop', tags='其他,综合,创意' WHERE id=49;
UPDATE image SET url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=800&h=600&fit=crop', thumbnail_url='https://images.unsplash.com/photo-1518791841217-8f3b3b3b3e3a?w=400&h=300&fit=crop', tags='其他,综合,生活' WHERE id=50;

-- 更新标题使其与分类匹配
UPDATE image SET title='壮丽山脉风光', description='连绵起伏的山脉在阳光下展现壮美景色' WHERE id=1;
UPDATE image SET title='宁静湖光山色', description='清澈的湖泊倒映着远山与森林' WHERE id=2;
UPDATE image SET title='金色日落余晖', description='夕阳染红天际，自然光影变幻无穷' WHERE id=3;
UPDATE image SET title='辽阔海岸线', description='蔚蓝海洋与天空相接的壮丽海岸' WHERE id=4;
UPDATE image SET title='翠绿山谷', description='层峦叠嶂的山谷中绿意盎然' WHERE id=5;
UPDATE image SET title='山涧瀑布', description='清澈溪流从山间倾泻而下' WHERE id=6;
UPDATE image SET title='田园风光', description='广袤田野展现乡村的宁静美好' WHERE id=7;

UPDATE image SET title='清新少女肖像', description='自然光下的女性肖像摄影' WHERE id=8;
UPDATE image SET title='商务男士特写', description='沉稳自信的男性肖像' WHERE id=9;
UPDATE image SET title='快乐童年', description='孩子天真无邪的笑容' WHERE id=10;
UPDATE image SET title='时尚女性街拍', description='都市时尚女性的穿搭展示' WHERE id=11;
UPDATE image SET title='职场精英', description='商务男士的职业形象' WHERE id=12;
UPDATE image SET title='甜蜜情侣', description='情侣日常生活的温馨瞬间' WHERE id=13;

UPDATE image SET title='慵懒猫咪', description='一只优雅的猫咪在窗边休憩' WHERE id=14;
UPDATE image SET title='忠诚金毛犬', description='金毛寻回犬的温暖笑容' WHERE id=15;
UPDATE image SET title='自由飞鸟', description='展翅翱翔的鸟类特写' WHERE id=16;
UPDATE image SET title='可爱兔子', description='毛茸茸的兔子在草地上' WHERE id=17;
UPDATE image SET title='骏马奔腾', description='草原上自由奔跑的骏马' WHERE id=18;
UPDATE image SET title='森林中的鹿', description='野生鹿在森林中静静伫立' WHERE id=19;

UPDATE image SET title='现代建筑群', description='几何线条构成的现代城市建筑' WHERE id=20;
UPDATE image SET title='摩天大楼', description='高耸入云的商务中心大厦' WHERE id=21;
UPDATE image SET title='欧式古典建筑', description='精雕细琢的欧洲古典建筑' WHERE id=22;
UPDATE image SET title='跨海大桥', description='工程奇迹般的跨海大桥' WHERE id=23;
UPDATE image SET title='室内空间设计', description='极简风格的室内建筑空间' WHERE id=24;
UPDATE image SET title='古老教堂', description='历史悠久的大教堂内部' WHERE id=25;

UPDATE image SET title='经典意式披萨', description='新鲜出炉的玛格丽特披萨' WHERE id=26;
UPDATE image SET title='健康轻食沙拉', description='色彩丰富的蔬菜沙拉碗' WHERE id=27;
UPDATE image SET title='美味汉堡', description='多汁牛肉配新鲜蔬菜的汉堡' WHERE id=28;
UPDATE image SET title='炭火烤肉', description='滋滋作响的烤肉拼盘' WHERE id=29;
UPDATE image SET title='精致甜点', description='装饰精美的法式蛋糕' WHERE id=30;
UPDATE image SET title='手冲咖啡', description='拉花拿铁配奶油蛋糕' WHERE id=31;

UPDATE image SET title='编程工作台', description='开发者的工作环境与代码' WHERE id=32;
UPDATE image SET title='智能手机', description='展示最新科技的手机产品' WHERE id=33;
UPDATE image SET title='精密电路板', description='电子元器件的微观世界' WHERE id=34;
UPDATE image SET title='数据中心', description='服务器机房的蓝色灯光' WHERE id=35;
UPDATE image SET title='智能机器人', description='人工智能机器人的未来感' WHERE id=36;
UPDATE image SET title='虚拟现实', description='VR设备带来的沉浸体验' WHERE id=37;

UPDATE image SET title='色彩抽象画', description='大胆色彩碰撞的抽象艺术作品' WHERE id=38;
UPDATE image SET title='现代雕塑', description='几何形态的现代雕塑作品' WHERE id=39;
UPDATE image SET title='创意抽象', description='充满想象力的抽象创作' WHERE id=40;
UPDATE image SET title='风景油画', description='传统风景油画的细腻笔触' WHERE id=41;
UPDATE image SET title='平面设计', description='精心排版的设计作品' WHERE id=42;
UPDATE image SET title='黑白摄影', description='光影对比强烈的黑白摄影' WHERE id=43;

UPDATE image SET title='生活百态', description='记录日常生活的综合影像' WHERE id=44;
UPDATE image SET title='旅行见闻', description='旅途中的所见所闻' WHERE id=45;
UPDATE image SET title='抽象意境', description='充满想象力的综合创作' WHERE id=46;
UPDATE image SET title='自然纹理', description='细腻的自然纹理与图案' WHERE id=47;
UPDATE image SET title='极简风格', description='简约至极的生活美学' WHERE id=48;
UPDATE image SET title='创意拼贴', description='多元元素融合的创意作品' WHERE id=49;
UPDATE image SET title='日常瞬间', description='捕捉生活中的美好瞬间' WHERE id=50;

-- 重置特征提取状态，需要重新提取
UPDATE image SET feature_extracted=0 WHERE id BETWEEN 1 AND 50;
