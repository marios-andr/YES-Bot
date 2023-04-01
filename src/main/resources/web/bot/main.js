let id = id => document.getElementById(id);

let responses = new Map([
    ["initialize", init],
    ["guilds", addGuildButtons],
    ["channels", addChannelOptions],
    ["channel_users", addUserOptions],
    ["console", onConsoleMessage]
])


ws.onmessage = msg => {
    let data = JSON.parse(msg.data);
    responses.get(data.type)(data);
};
ws.onopen = () => {

};
ws.onclose = () => {
    alert("Connection closed.")
};

function init(data) {
    ws.send(JSON.stringify({
        type: "guilds"
    }));
}

let currentGuild;
let guilds = [];
let guildDelta = 0;

function addGuildButtons(data) {
    let d = id("guilds");


    data.guilds.forEach(e => {
        guilds.push(e);
    });

    for (let i = guildDelta; i < Math.min(guilds.length, 5 + guildDelta); i++) {
        let e = guilds[i];
        let button = document.createElement("button");
        button.type = "button"
        button.addEventListener("click", function () {
            currentGuild = e.id;
            ws.send(JSON.stringify({
                type: "channels",
                guildId: e.id
            }));
        });
        d.appendChild(button);

        let img = document.createElement("img");
        img.className = "guild_button_img";
        img.src = e.image;
        img.alt = e.name;
        button.appendChild(img);
    }

    for (let i = 0; i < 5 - guilds.length; i++) {
        let img = document.createElement("div");
        img.className = "guild_button_img";
        d.appendChild(img);
    }
}

function addChannelOptions(data) {
    let t = id("text_channel_select");
    let v = id("voice_channel_select");

    t.removeAttribute("hidden")
    v.removeAttribute("hidden")
    data.channels.forEach(e => {
        let option = document.createElement("option");
        option.innerText = e.name;
        option.value = e.id;

        if (e.type === "text") {
            t.appendChild(option);
        } else if (e.type === "voice") {
            v.appendChild(option);
        }
    });

    onTextChannelSelect();
    onVoiceChannelSelect();
}

function addUserOptions(data) {
    let users = id("users_select");

    users.removeAttribute("hidden");
    data.users.forEach(e => {
        let option = document.createElement("option");
        option.innerText = e.name;
        option.value = e.mention;

        users.appendChild(option);
    })
}

function onConsoleMessage(data) {
    let console = id("console");

    console.value += data.msg + "\r\n";
}

function onMoveButtonPress(direction) {
    if (direction === 'left' && guildDelta > 0)
        guildDelta--;
    else if (direction === 'right' && 5 + guildDelta < guilds.length)
        guildDelta++;
}

function onTextChannelSelect() {
    let area = id("message_area");
    let btn = id("send_button");

    let channel = id("text_channel_select");
    ws.send(JSON.stringify({
        type: "channel_users",
        guildId: currentGuild,
        channelId: channel.value
    }))

    area.removeAttribute("hidden");
    btn.removeAttribute("hidden");
}

function onVoiceChannelSelect() {
    let j = id("voice_join_button");
    let l = id("voice_leave_button");
    let u = id("voice_unmuteall_button");
    let m = id("voice_muteall_button");

    j.removeAttribute("hidden");
    l.removeAttribute("hidden");
    u.removeAttribute("hidden");
    m.removeAttribute("hidden");
}

function onUserSelect() {
    let area = id("message_area");
    let users = id("users_select");

    let mention = users.value;
    area.value += mention;
}

function onSendButtonPress() {
    let area = id("message_area");
    let channel = id("text_channel_select");

    ws.send(JSON.stringify({
        type: "send_msg",
        msg: area.value,
        guildId: currentGuild,
        channelId: channel.value
    }));
}

function onVoiceJoinPress() {
    let channel = id("voice_channel_select");

    ws.send(JSON.stringify({
        type: "voice_join",
        guildId: currentGuild,
        channelId: channel.value
    }));
}

function onVoiceLeavePress() {
    let channel = id("voice_channel_select");

    ws.send(JSON.stringify({
        type: "voice_leave",
        guildId: currentGuild,
        channelId: channel.value
    }));
}

function onVoiceMuteAllPress() {
    let channel = id("voice_channel_select");

    ws.send(JSON.stringify({
        type: "voice_mute_all",
        guildId: currentGuild,
        channelId: channel.value
    }));
}

function onVoiceUnmuteAllPress() {
    let channel = id("voice_channel_select");

    ws.send(JSON.stringify({
        type: "voice_unmute_all",
        guildId: currentGuild,
        channelId: channel.value
    }));
}

function onLockPress() {
    ws.send(JSON.stringify({
        type: "lock",
        lock: true
    }));
}

function onUnlockPress() {
    ws.send(JSON.stringify({
        type: "lock",
        lock: false
    }));
}

function onStopPress() {
    let flag = confirm("Are you sure you wish to shutdown the bot?");

    if (flag) {
        ws.send(JSON.stringify({
            type: "shutdown"
        }));
    }
}

function onReloadPress() {
    ws.send(JSON.stringify({
        type: "refresh"
    }));
}
