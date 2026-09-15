-- 按分类更新图片URL，使用Pexels真实图片（已验证全部可访问）
-- 分类：1=风景 2=人物 3=动物 4=建筑 5=美食 6=科技 7=艺术 8=其他

-- 风景类 (category_id=1, image id 1-7)
UPDATE image SET url='https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='风景,自然,山脉' WHERE id=1;
UPDATE image SET url='https://images.pexels.com/photos/1366919/pexels-photo-1366919.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1366919/pexels-photo-1366919.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='风景,湖泊,森林' WHERE id=2;
UPDATE image SET url='https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='风景,日落,自然' WHERE id=3;
UPDATE image SET url='https://images.pexels.com/photos/1671324/pexels-photo-1671324.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1671324/pexels-photo-1671324.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='风景,海洋,海岸' WHERE id=4;
UPDATE image SET url='https://images.pexels.com/photos/2405208/pexels-photo-2405208.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/2405208/pexels-photo-2405208.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='风景,山谷,绿色' WHERE id=5;
UPDATE image SET url='https://images.pexels.com/photos/572897/pexels-photo-572897.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/572897/pexels-photo-572897.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='风景,山野,云海' WHERE id=6;
UPDATE image SET url='https://images.pexels.com/photos/1770809/pexels-photo-1770809.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1770809/pexels-photo-1770809.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='风景,日出,山峦' WHERE id=7;

-- 人物类 (category_id=2, image id 8-13)
UPDATE image SET url='https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='人物,女性,肖像' WHERE id=8;
UPDATE image SET url='https://images.pexels.com/photos/3777943/pexels-photo-3777943.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/3777943/pexels-photo-3777943.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='人物,男性,肖像' WHERE id=9;
UPDATE image SET url='https://images.pexels.com/photos/736710/pexels-photo-736710.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/736710/pexels-photo-736710.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='人物,儿童,快乐' WHERE id=10;
UPDATE image SET url='https://images.pexels.com/photos/614810/pexels-photo-614810.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/614810/pexels-photo-614810.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='人物,女性,时尚' WHERE id=11;
UPDATE image SET url='https://images.pexels.com/photos/91227/pexels-photo-91227.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/91227/pexels-photo-91227.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='人物,男性,商务' WHERE id=12;
UPDATE image SET url='https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='人物,情侣,生活' WHERE id=13;

-- 动物类 (category_id=3, image id 14-19)
UPDATE image SET url='https://images.pexels.com/photos/2253275/pexels-photo-2253275.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/2253275/pexels-photo-2253275.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='动物,狗,宠物' WHERE id=14;
UPDATE image SET url='https://images.pexels.com/photos/247502/pexels-photo-247502.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/247502/pexels-photo-247502.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='动物,猫,宠物' WHERE id=15;
UPDATE image SET url='https://images.pexels.com/photos/45953/pexels-photo-45953.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/45953/pexels-photo-45953.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='动物,野生动物,自然' WHERE id=16;
UPDATE image SET url='https://images.pexels.com/photos/247937/pexels-photo-247937.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/247937/pexels-photo-247937.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='动物,马,草原' WHERE id=17;
UPDATE image SET url='https://images.pexels.com/photos/2246277/pexels-photo-2246277.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/2246277/pexels-photo-2246277.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='动物,兔子,可爱' WHERE id=18;
UPDATE image SET url='https://images.pexels.com/photos/3364400/pexels-photo-3364400.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/3364400/pexels-photo-3364400.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='动物,鸟,自由' WHERE id=19;

-- 建筑类 (category_id=4, image id 20-25)
UPDATE image SET url='https://images.pexels.com/photos/323780/pexels-photo-323780.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/323780/pexels-photo-323780.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='建筑,现代,城市' WHERE id=20;
UPDATE image SET url='https://images.pexels.com/photos/302769/pexels-photo-302769.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/302769/pexels-photo-302769.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='建筑,高楼,商务' WHERE id=21;
UPDATE image SET url='https://images.pexels.com/photos/280215/pexels-photo-280215.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/280215/pexels-photo-280215.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='建筑,古典,欧式' WHERE id=22;
UPDATE image SET url='https://images.pexels.com/photos/534220/pexels-photo-534220.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/534220/pexels-photo-534220.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='建筑,桥梁,工程' WHERE id=23;
UPDATE image SET url='https://images.pexels.com/photos/803975/pexels-photo-803975.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/803975/pexels-photo-803975.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='建筑,室内,设计' WHERE id=24;
UPDATE image SET url='https://images.pexels.com/photos/259605/pexels-photo-259605.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/259605/pexels-photo-259605.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='建筑,教堂,历史' WHERE id=25;

-- 美食类 (category_id=5, image id 26-31)
UPDATE image SET url='https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='美食,沙拉,健康' WHERE id=26;
UPDATE image SET url='https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='美食,披萨,意式' WHERE id=27;
UPDATE image SET url='https://images.pexels.com/photos/1639557/pexels-photo-1639557.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1639557/pexels-photo-1639557.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='美食,汉堡,快餐' WHERE id=28;
UPDATE image SET url='https://images.pexels.com/photos/1351238/pexels-photo-1351238.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1351238/pexels-photo-1351238.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='美食,甜点,蛋糕' WHERE id=29;
UPDATE image SET url='https://images.pexels.com/photos/540900/pexels-photo-540900.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/540900/pexels-photo-540900.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='美食,咖啡,饮品' WHERE id=30;
UPDATE image SET url='https://images.pexels.com/photos/461198/pexels-photo-461198.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/461198/pexels-photo-461198.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='美食,烧烤,烤肉' WHERE id=31;

-- 科技类 (category_id=6, image id 32-37)
UPDATE image SET url='https://images.pexels.com/photos/270700/pexels-photo-270700.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/270700/pexels-photo-270700.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='科技,电脑,编程' WHERE id=32;
UPDATE image SET url='https://images.pexels.com/photos/356056/pexels-photo-356056.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/356056/pexels-photo-356056.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='科技,手机,智能' WHERE id=33;
UPDATE image SET url='https://images.pexels.com/photos/546819/pexels-photo-546819.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/546819/pexels-photo-546819.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='科技,电路,电子' WHERE id=34;
UPDATE image SET url='https://images.pexels.com/photos/777001/pexels-photo-777001.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/777001/pexels-photo-777001.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='科技,服务器,数据中心' WHERE id=35;
UPDATE image SET url='https://images.pexels.com/photos/8386440/pexels-photo-8386440.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/8386440/pexels-photo-8386440.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='科技,机器人,智能' WHERE id=36;
UPDATE image SET url='https://images.pexels.com/photos/1029756/pexels-photo-1029756.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1029756/pexels-photo-1029756.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='科技,VR,虚拟现实' WHERE id=37;

-- 艺术类 (category_id=7, image id 38-43)
UPDATE image SET url='https://images.pexels.com/photos/1183992/pexels-photo-1183992.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1183992/pexels-photo-1183992.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='艺术,绘画,色彩' WHERE id=38;
UPDATE image SET url='https://images.pexels.com/photos/3263114/pexels-photo-3263114.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/3263114/pexels-photo-3263114.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='艺术,雕塑,现代' WHERE id=39;
UPDATE image SET url='https://images.pexels.com/photos/1190906/pexels-photo-1190906.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1190906/pexels-photo-1190906.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='艺术,抽象,创意' WHERE id=40;
UPDATE image SET url='https://images.pexels.com/photos/876423/pexels-photo-876423.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/876423/pexels-photo-876423.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='艺术,油画,风景' WHERE id=41;
UPDATE image SET url='https://images.pexels.com/photos/3136910/pexels-photo-3136910.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/3136910/pexels-photo-3136910.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='艺术,设计,排版' WHERE id=42;
UPDATE image SET url='https://images.pexels.com/photos/1269968/pexels-photo-1269968.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1269968/pexels-photo-1269968.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='艺术,绘画,创作' WHERE id=43;

-- 其他类 (category_id=8, image id 44-50)
UPDATE image SET url='https://images.pexels.com/photos/1262300/pexels-photo-1262300.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1262300/pexels-photo-1262300.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='其他,综合,生活' WHERE id=44;
UPDATE image SET url='https://images.pexels.com/photos/1293241/pexels-photo-1293241.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1293241/pexels-photo-1293241.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='其他,综合,旅行' WHERE id=45;
UPDATE image SET url='https://images.pexels.com/photos/1670909/pexels-photo-1670909.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1670909/pexels-photo-1670909.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='其他,综合,抽象' WHERE id=46;
UPDATE image SET url='https://images.pexels.com/photos/1670871/pexels-photo-1670871.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/1670871/pexels-photo-1670871.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='其他,综合,纹理' WHERE id=47;
UPDATE image SET url='https://images.pexels.com/photos/762020/pexels-photo-762020.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/762020/pexels-photo-762020.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='其他,综合,极简' WHERE id=48;
UPDATE image SET url='https://images.pexels.com/photos/207216/pexels-photo-207216.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/207216/pexels-photo-207216.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='其他,自然,创意' WHERE id=49;
UPDATE image SET url='https://images.pexels.com/photos/207217/pexels-photo-207217.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop', thumbnail_url='https://images.pexels.com/photos/207217/pexels-photo-207217.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop', tags='其他,自然,生活' WHERE id=50;

-- 更新标题使其与分类匹配
UPDATE image SET title='壮丽山脉风光', description='连绵起伏的山脉在阳光下展现壮美景色' WHERE id=1;
UPDATE image SET title='宁静湖光山色', description='清澈的湖泊倒映着远山与森林' WHERE id=2;
UPDATE image SET title='金色日落余晖', description='夕阳染红天际，自然光影变幻无穷' WHERE id=3;
UPDATE image SET title='辽阔海岸线', description='蔚蓝海洋与天空相接的壮丽海岸' WHERE id=4;
UPDATE image SET title='翠绿山谷', description='层峦叠嶂的山谷中绿意盎然' WHERE id=5;
UPDATE image SET title='田园风光', description='广袤田野展现乡村的宁静美好' WHERE id=6;
UPDATE image SET title='云海日出', description='清晨阳光穿过云层照亮群山' WHERE id=7;

UPDATE image SET title='清新少女肖像', description='自然光下的女性肖像摄影' WHERE id=8;
UPDATE image SET title='商务男士特写', description='沉稳自信的男性肖像' WHERE id=9;
UPDATE image SET title='快乐童年', description='孩子天真无邪的笑容' WHERE id=10;
UPDATE image SET title='时尚女性街拍', description='都市时尚女性的穿搭展示' WHERE id=11;
UPDATE image SET title='职场精英', description='商务男士的职业形象' WHERE id=12;
UPDATE image SET title='甜蜜情侣', description='情侣日常生活的温馨瞬间' WHERE id=13;

UPDATE image SET title='忠诚金毛犬', description='金毛寻回犬的温暖笑容' WHERE id=14;
UPDATE image SET title='慵懒猫咪', description='一只优雅的猫咪在窗边休憩' WHERE id=15;
UPDATE image SET title='野生动物', description='自然界中的野生动物特写' WHERE id=16;
UPDATE image SET title='骏马奔腾', description='草原上自由奔跑的骏马' WHERE id=17;
UPDATE image SET title='可爱兔子', description='毛茸茸的兔子在草地上' WHERE id=18;
UPDATE image SET title='自由飞鸟', description='展翅翱翔的鸟类特写' WHERE id=19;

UPDATE image SET title='现代建筑群', description='几何线条构成的现代城市建筑' WHERE id=20;
UPDATE image SET title='摩天大楼', description='高耸入云的商务中心大厦' WHERE id=21;
UPDATE image SET title='欧式古典建筑', description='精雕细琢的欧洲古典建筑' WHERE id=22;
UPDATE image SET title='跨海大桥', description='工程奇迹般的跨海大桥' WHERE id=23;
UPDATE image SET title='室内空间设计', description='极简风格的室内建筑空间' WHERE id=24;
UPDATE image SET title='古老教堂', description='历史悠久的大教堂建筑' WHERE id=25;

UPDATE image SET title='健康轻食沙拉', description='色彩丰富的蔬菜沙拉碗' WHERE id=26;
UPDATE image SET title='经典意式披萨', description='新鲜出炉的玛格丽特披萨' WHERE id=27;
UPDATE image SET title='美味汉堡', description='多汁牛肉配新鲜蔬菜的汉堡' WHERE id=28;
UPDATE image SET title='精致甜点', description='装饰精美的法式蛋糕' WHERE id=29;
UPDATE image SET title='手冲咖啡', description='拉花拿铁配奶油蛋糕' WHERE id=30;
UPDATE image SET title='炭火烤肉', description='滋滋作响的烤肉拼盘' WHERE id=31;

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
UPDATE image SET title='艺术创作', description='富有表现力的艺术绘画作品' WHERE id=43;

UPDATE image SET title='生活百态', description='记录日常生活的综合影像' WHERE id=44;
UPDATE image SET title='旅行见闻', description='旅途中的所见所闻' WHERE id=45;
UPDATE image SET title='抽象意境', description='充满想象力的综合创作' WHERE id=46;
UPDATE image SET title='自然纹理', description='细腻的自然纹理与图案' WHERE id=47;
UPDATE image SET title='极简风格', description='简约至极的生活美学' WHERE id=48;
UPDATE image SET title='自然创意', description='自然元素构成的创意画面' WHERE id=49;
UPDATE image SET title='日常光景', description='捕捉生活中的自然瞬间' WHERE id=50;

-- 重置特征提取状态
UPDATE image SET feature_extracted=0 WHERE id BETWEEN 1 AND 50;
